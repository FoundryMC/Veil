package foundry.veil.api.client.editor;

import foundry.veil.api.client.render.VeilRenderSystem;
import imgui.ImGui;
import imgui.type.ImBoolean;

/**
 * Displays a single window as the editor. Automatically handles the close widget in the corner.
 *
 * @author Ocelot
 */
public abstract class SingleWindowEditor implements Editor {

    protected final ImBoolean open = new ImBoolean();

    @Override
    public void render() {
        if (ImGui.begin(this.getWindowTitle(), this.open)) {
            this.renderComponents();
        }
        ImGui.end();

        if (!this.open.get()) {
            VeilRenderSystem.renderer().getEditorManager().hide(this);
        }
    }

    @Override
    public void onShow() {
        this.open.set(true);
    }

    /**
     * Adds all components inside the window.
     */
    protected abstract void renderComponents();

    /**
     * @return The title of the window
     */
    protected String getWindowTitle() {
        return this.getDisplayName();
    }
}
