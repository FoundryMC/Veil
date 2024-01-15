package foundry.veil.impl.client.editor;

import foundry.veil.api.client.editor.Editor;
import imgui.ImGui;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
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
