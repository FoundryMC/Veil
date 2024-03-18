package foundry.veil.impl.client.editor;

import foundry.veil.api.client.editor.Editor;
import foundry.veil.api.client.render.VeilRenderSystem;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class DemoEditor implements Editor {

    private final ImBoolean open = new ImBoolean();

    @Override
    public void render() {
        ImGui.showDemoWindow(this.open);

        if (!this.open.get()) {
            VeilRenderSystem.renderer().getEditorManager().hide(this);
        }
    }

    @Override
    public void onShow() {
        this.open.set(true);
    }

    @Override
    public String getDisplayName() {
        return "Dear ImGui Demo";
    }
}
