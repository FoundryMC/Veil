package foundry.veil.editor;

import imgui.ImGui;

public class ExampleEditor implements Editor {

    @Override
    public void renderMenuBar() {
        ImGui.menuItem("Example 1");
        ImGui.menuItem("Example 2");
    }

    @Override
    public void render() {
        ImGui.showDemoWindow();
    }

    @Override
    public boolean isMenuBarEnabled() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Example";
    }
}
