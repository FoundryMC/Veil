package foundry.veil.imgui;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class InactiveVeilImGuiImpl implements VeilImGui {

    @Override
    public void begin() {
    }

    @Override
    public void end() {
    }

    @Override
    public void windowFocusCallback(long window, boolean focused) {
    }

    @Override
    public void cursorEnterCallback(long window, boolean entered) {
    }

    @Override
    public void mouseButtonCallback(long window, int button, int action, int mods) {
    }

    @Override
    public void scrollCallback(long window, double xOffset, double yOffset) {
    }

    @Override
    public void keyCallback(long window, int key, int scancode, int action, int mods) {
    }

    @Override
    public void charCallback(long window, int codepoint) {
    }

    @Override
    public void free() {
    }
}
