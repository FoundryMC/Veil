package foundry.veil.imgui;

import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

/**
 * Manages the internal ImGui state.
 */
@ApiStatus.Internal
public class VeilImGui implements NativeResource {

    private final long window;
    private final ImGuiImplGlfw implGlfw;
    private final ImGuiImplGl3 implGl3;

    public VeilImGui(long window) {
        this.window = window;
        this.implGlfw = new ImGuiImplGlfw();
        this.implGl3 = new ImGuiImplGl3();

        ImGui.createContext();
        this.implGlfw.init(window, false);
        this.implGl3.init("#version 410 core");
    }

    public void begin() {
        this.implGlfw.newFrame();
        ImGui.newFrame();

        // Test code
        ImGui.begin("Test");
        ImGui.text("string cheese");
        ImGui.end();
    }

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

    public ImGuiImplGlfw getImplGlfw() {
        return this.implGlfw;
    }

    public long getWindow() {
        return this.window;
    }

    @Override
    public void free() {
        this.implGlfw.dispose();
        this.implGl3.dispose();
        ImGui.destroyContext();
    }
}
