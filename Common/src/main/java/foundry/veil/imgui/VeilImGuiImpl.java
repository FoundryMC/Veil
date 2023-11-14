package foundry.veil.imgui;

import foundry.veil.Veil;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.jetbrains.annotations.ApiStatus;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

/**
 * Manages the internal ImGui state.
 */
@ApiStatus.Internal
public class VeilImGuiImpl implements VeilImGui {

    private static VeilImGui instance;

    private final long window;
    private final ImGuiImplGlfw implGlfw;
    private final ImGuiImplGl3 implGl3;

    private VeilImGuiImpl(long window) {
        this.window = window;
        this.implGlfw = new ImGuiImplGlfw();
        this.implGl3 = new ImGuiImplGl3();

        ImGui.createContext();
        this.implGlfw.init(window, false);
        this.implGl3.init("#version 410 core");
    }

    @Override
    public void begin() {
        this.implGlfw.newFrame();
        ImGui.newFrame();

        // Test code
        ImGui.begin("Test");
        ImGui.text("string cheese");
        ImGui.end();
    }

    @Override
    public void end() {
        ImGui.render();
        this.implGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            glfwMakeContextCurrent(backupWindowPtr);
        }
    }

    @Override
    public void windowFocusCallback(long window, boolean focused) {
        if (this.window == window) {
            this.implGlfw.windowFocusCallback(window, focused);
        }
    }

    @Override
    public void cursorEnterCallback(long window, boolean entered) {
        if (this.window == window) {
            this.implGlfw.cursorEnterCallback(window, entered);
        }
    }

    @Override
    public void mouseButtonCallback(long window, int button, int action, int mods) {
        if (this.window == window) {
            this.implGlfw.mouseButtonCallback(window, button, action, mods);
        }
    }

    @Override
    public void scrollCallback(long window, double xOffset, double yOffset) {
        if (this.window == window) {
            this.implGlfw.scrollCallback(window, xOffset, yOffset);
        }
    }

    @Override
    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (this.window == window) {
            this.implGlfw.keyCallback(window, key, scancode, action, mods);
        }
    }

    @Override
    public void charCallback(long window, int codepoint) {
        if (this.window == window) {
            this.implGlfw.charCallback(window, codepoint);
        }
    }

    @Override
    public void free() {
        this.implGlfw.dispose();
        this.implGl3.dispose();
        ImGui.destroyContext();
    }

    public static void init(long window) {
        instance = Veil.IMGUI ? new VeilImGuiImpl(window) : new InactiveVeilImGuiImpl();
    }

    public static VeilImGui get() {
        return instance;
    }
}
