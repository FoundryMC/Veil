package foundry.veil.api.client.editor;

/**
 * Provides extra ImGui rendering details. This is used by editor panels to add/change properties with ImGui.
 *
 * @author Ocelot
 */
public interface EditorAttributeProvider {

    /**
     * Renders all ImGui attributes into the current editor panel.
     */
    void renderImGuiAttributes();
}
