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
    public boolean mouseButtonCallback(long window, int button, int action, int mods) {
        return false;
    }

    @Override
    public boolean scrollCallback(long window, double xOffset, double yOffset) {
        return false;
    }

    @Override
    public boolean keyCallback(long window, int key, int scancode, int action, int mods) {
        return false;
    }

    @Override
    public boolean charCallback(long window, int codepoint) {
        return false;
    }

    @Override
    public boolean shouldHideMouse() {
        return false;
    }

    @Override
    public void free() {
    }
}
