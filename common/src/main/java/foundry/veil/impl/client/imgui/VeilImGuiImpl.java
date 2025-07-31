package foundry.veil.impl.client.imgui;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import imgui.ImGui;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.internal.ImGuiContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.ObjIntConsumer;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

/**
 * Manages the internal ImGui state.
 */
@ApiStatus.Internal
public class VeilImGuiImpl implements VeilImGui, NativeResource {

    private static final MethodHandle DATA_GETTER;
    private static final MethodHandle SHADER_GETTER;

    static {
        MethodHandle dataGetter;
        MethodHandle shaderGetter;

        try {
            Class<?> dataClass = Class.forName("imgui.gl3.ImGuiImplGl3$Data");
            MethodHandles.Lookup dataLookup = MethodHandles.privateLookupIn(ImGuiImplGl3.class, MethodHandles.lookup());
            dataGetter = dataLookup.findGetter(ImGuiImplGl3.class, "data", dataClass);

            MethodHandles.Lookup shaderLookup = MethodHandles.privateLookupIn(dataClass, dataLookup);
            shaderGetter = shaderLookup.findGetter(dataClass, "shaderHandle", int.class);
        } catch (Throwable t) {
            Veil.LOGGER.error("Failed to get ImGui shader handle: {}", t.getMessage());
            dataGetter = null;
            shaderGetter = null;
        }

        DATA_GETTER = dataGetter;
        SHADER_GETTER = shaderGetter;
    }

    private static VeilImGui instance = new InactiveVeilImGuiImpl();

    private final VeilImGuiImplGlfw implGlfw;
    private final ImGuiImplGl3 implGl3;
    private final ImGuiContext imGuiContext;
    private final ImPlotContext imPlotContext;
    private final AtomicBoolean active;

    private VeilImGuiImpl(long window) throws Throwable {
        this.implGlfw = new VeilImGuiImplGlfw(this);
        this.implGl3 = new ImGuiImplGl3();

        ImGuiStateStack.push();
        ImGuiContext imGuiContext = null;
        ImPlotContext imPlotContext = null;
        try {
            imGuiContext = ImGui.createContext();
            imPlotContext = ImPlot.createContext();
            this.active = new AtomicBoolean();
            this.implGl3.init("#version 410 core");
            this.implGlfw.init(window, true);

            VeilImGuiStylesheet.initStyles();
        } catch (Throwable t) {
            // Make sure nothing leaks when an error occurs
            this.implGlfw.shutdown();
            this.implGl3.destroyDeviceObjects();
            if (imGuiContext != null) {
                ImGui.destroyContext(imGuiContext);
            }
            if (imPlotContext != null) {
                ImPlot.destroyContext(imPlotContext);
            }
            throw t;
        } finally {
            ImGuiStateStack.forcePop();
        }
        this.imGuiContext = imGuiContext;
        this.imPlotContext = imPlotContext;
    }

    @Override
    public void start() {
        ImGuiStateStack.push();
        ImGui.setCurrentContext(this.imGuiContext);
        ImPlot.setCurrentContext(this.imPlotContext);

        // Sanity check
        if (ImGui.getCurrentContext().isNotValidPtr()) {
            throw new IllegalStateException("ImGui Context is not valid");
        }
        // These callbacks MUST be called from the main thread
        RenderSystem.assertOnRenderThread();
    }

    @Override
    public void stop() {
        RenderSystem.assertOnRenderThread();
        ImGuiStateStack.pop();
    }

    @Override
    public void beginFrame() {
        try {
            this.start();

            if (this.active.get()) {
                Veil.LOGGER.error("ImGui failed to render previous frame, disposing");
                ImGui.endFrame();
            }
            this.active.set(true);
            this.implGl3.newFrame();
            this.implGlfw.newFrame();
            ImGui.newFrame();

            AdvancedFboImGuiAreaImpl.begin();
            VeilRenderSystem.renderer().getEditorManager().render();
        } finally {
            this.stop();
        }
    }

    @Override
    public void endFrame() {
        AdvancedFboImGuiAreaImpl.end();

        try {
            if (!this.active.get()) {
                Veil.LOGGER.error("ImGui state de-synced");
                return;
            }

            this.start();

            this.active.set(false);
            VeilRenderSystem.renderer().getEditorManager().renderLast();
            ImGui.render();
            this.implGl3.renderDrawData(ImGui.getDrawData());

            if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
                final long backupWindowPtr = glfwGetCurrentContext();
                ImGui.updatePlatformWindows();
                ImGui.renderPlatformWindowsDefault();
                glfwMakeContextCurrent(backupWindowPtr);
            }
        } finally {
            ImGuiStateStack.forcePop();
        }
    }

    @Override
    public void toggle() {
        VeilRenderSystem.renderer().getEditorManager().toggle();
    }

    @Override
    public void updateFonts() {
        this.implGl3.destroyFontsTexture();
        if (!this.implGl3.createFontsTexture()) {
            throw new IllegalStateException("Failed to update font texture");
        }
    }

    @Override
    public void addImguiShaders(ObjIntConsumer<ResourceLocation> registry) {
        if (DATA_GETTER != null && SHADER_GETTER != null) {
            try {
                int handle = (int) SHADER_GETTER.invoke(DATA_GETTER.invoke(this.implGl3));
                registry.accept(ResourceLocation.fromNamespaceAndPath("imgui", "blit"), handle);
            } catch (Throwable t) {
                Veil.LOGGER.warn("Failed to add ImGui shader", t);
            }
        }
    }

    @Override
    public void free() {
        try {
            this.start();
            this.implGlfw.shutdown();
            this.implGl3.destroyDeviceObjects();
            ImGui.destroyContext(this.imGuiContext);
            ImPlot.destroyContext(this.imPlotContext);
        } finally {
            this.stop();
        }
    }

    public static void init(long window) {
        try {
            instance = Veil.IMGUI ? new VeilImGuiImpl(window) : new InactiveVeilImGuiImpl();
        } catch (Throwable t) {
            Veil.LOGGER.error("Failed to load ImGui, disabling", t);
            instance = new InactiveVeilImGuiImpl();
        }
    }

    public static void setImGuiPath() {
//        if (System.getProperty("os.arch").equals("arm") || System.getProperty("os.arch").startsWith("aarch64")) {
//            // ImGui infers a path for loading the library using this name property
//            // Essential that this property is set, before any ImGui-adjacent native code is loaded
//            System.setProperty("imgui.library.name", "libimgui-javaarm64.dylib");
//        }
    }

    public static VeilImGui get() {
        return instance;
    }
}
