package foundry.veil.editor;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Manages all editors for Veil. Editors are ImGui powered panels that can be dynamically registered and unregistered with {@link #add(Editor)}.</p>
 *
 * @author Ocelot
 */
public class EditorManager {

    private final Map<Editor, ImBoolean> editors;

    @ApiStatus.Internal
    public EditorManager() {
        this.editors = new HashMap<>();

        // debug editors
        this.add(new ExampleEditor());
        this.add(new PostEditor());
    }

    @ApiStatus.Internal
    public void render() {
        if (!ImGui.beginMainMenuBar()) {
            return;
        }

        if (ImGui.beginMenu("Editor")) {
            for (Map.Entry<Editor, ImBoolean> entry : this.editors.entrySet()) {
                Editor editor = entry.getKey();
                ImBoolean enabled = entry.getValue();

                ImGui.beginDisabled(!editor.isEnabled());
                if (ImGui.menuItem(editor.getDisplayName(), null, enabled.get())) {
                    if (!enabled.get()) {
                        this.show(editor);
                    } else {
                        this.hide(editor);
                    }
                }
                ImGui.endDisabled();

                if (!editor.isEnabled()) {
                    enabled.set(false);
                }
            }
            ImGui.endMenu();
        }

        for (Map.Entry<Editor, ImBoolean> entry : this.editors.entrySet()) {
            Editor editor = entry.getKey();
            if (entry.getValue().get() && editor.isMenuBarEnabled()) {
                ImGui.separator();
                ImGui.textColored(0xFFAAAAAA, editor.getDisplayName());
                editor.renderMenuBar();
            }
        }

        ImGui.endMainMenuBar();

        for (Map.Entry<Editor, ImBoolean> entry : this.editors.entrySet()) {
            Editor editor = entry.getKey();
            ImBoolean enabled = entry.getValue();

            if (!enabled.get()) {
                continue;
            }

            editor.render();
        }
    }

    public void show(Editor editor) {
        ImBoolean enabled = this.editors.get(editor);
        if (enabled != null && !enabled.get()) {
            editor.onShow();
            enabled.set(true);
        }
    }

    public void hide(Editor editor) {
        ImBoolean enabled = this.editors.get(editor);
        if (enabled != null && enabled.get()) {
            editor.onHide();
            enabled.set(false);
        }
    }

    public void show() {
        // TODO
    }

    public void hide() {
    }

    public synchronized void add(Editor editor) {
        this.editors.computeIfAbsent(editor, unused -> new ImBoolean());
    }

    public synchronized void remove(Editor editor) {
        this.hide(editor);
        this.editors.remove(editor);
    }
}
