package foundry.veil.api.client.imgui;

import foundry.imgui.api.ImGuiMC;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.texteditor.TextEditor;
import imgui.extension.texteditor.TextEditorCursorPosition;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;

/**
 * Creates a text editor window with saving callback support.
 *
 * @author Ocelot
 */
public class CodeEditor implements NativeResource {

    private static final Component EDITOR = Component.translatable("editor.veil.default");

    // TODO reimplement in Java
    private final Component name;
    private final TextEditor editor;
    private final Component saveText;
    private CharSequence oldSource;
    private SaveCallback saveCallback;
    private String fileName;

    private final ImBoolean open;

    public CodeEditor(@Nullable Component saveText) {
        this(EDITOR, saveText);
    }

    public CodeEditor(Component name, @Nullable Component saveText) {
        this.name = name;
        this.editor = new TextEditor();
        this.editor.setShowWhitespacesEnabled(false);
        this.saveText = saveText;
        this.oldSource = null;
        this.saveCallback = null;

        this.open = new ImBoolean();
    }

    /**
     * @return Whether the text has changed since last save
     */
    public boolean hasTextChanged() {
        return this.oldSource != null && !this.oldSource.equals(this.editor.getText());
    }

    /**
     * Fires the save callback if the text has changed.
     */
    public void save() {
        try (final ImGuiMC.ActiveContext context = ImGuiMC.withImGui()) {
            if (context != null) {
                if (this.saveCallback != null) {
                    this.saveCallback.save(this.editor.getText(), this.editor::addMarker);
                }
                if (this.editor.hasMarkers()) {
                    this.oldSource = this.editor.getText();
                }
            }
        }
    }

    /**
     * Shows the editor with the specified source.
     *
     * @param source The source to display
     */
    public void show(@Nullable String fileName, String source) {
        this.editor.setText(source);
        this.fileName = fileName;
        this.oldSource = this.editor.getText();
        this.editor.clearMarkers();
        this.open.set(true);
        try (final ImGuiMC.ActiveContext context = ImGuiMC.withImGui()) {
            if (context != null) {
                ImGui.setWindowFocus("###editor");
                ImGui.setWindowCollapsed("###editor", false);
            }
        }
    }

    /**
     * Attempts to hide and save the editor.
     */
    public void hide() {
        if (this.hasTextChanged()) {
            this.open.set(true);
            try (final ImGuiMC.ActiveContext context = ImGuiMC.withImGui()) {
                if (context != null) {
                    ImGui.pushID(this.hashCode());
                    ImGui.openPopup("###save_confirm");
                    ImGui.popID();
                }
            }
        } else {
            this.oldSource = null;
            this.open.set(false);
        }
    }

    /**
     * Renders the editor in a closable window.
     */
    public void renderWindow() {
        int flags = ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoSavedSettings;
        if (!this.editor.isReadOnlyEnabled() && this.hasTextChanged()) {
            flags |= ImGuiWindowFlags.UnsavedDocument;
        }

        if (!this.open.get()) {
            return;
        }

        int id = this.hashCode();
        ImGui.pushID(id);
        ImGui.setNextWindowSizeConstraints(800, 600, Float.MAX_VALUE, Float.MAX_VALUE);
        if (ImGui.begin(this.name.getString() + (this.fileName != null ? ": " + this.fileName : "") + "###editor" + id, this.open, flags)) {
            this.render();
        }

        if (!this.open.get()) {
            this.hide();
        }

        ImGui.end();
        ImGui.popID();
    }

    /**
     * Renders the editor onto the stack.
     */
    public void render() {
        ImGui.pushID(this.hashCode());
        if (this.open.get()) {
            if (!this.hasTextChanged()) {
                this.editor.clearMarkers();
            }

            if (ImGui.beginMenuBar()) {
                boolean immutable = this.editor.isReadOnlyEnabled();
                if (ImGui.menuItem("Read-only mode", "", immutable)) {
                    this.editor.setReadOnlyEnabled(!immutable);
                }
                if (ImGui.menuItem("Show Whitespace", "", this.editor.isShowWhitespacesEnabled())) {
                    this.editor.setShowWhitespacesEnabled(!this.editor.isShowWhitespacesEnabled());
                }

                if (this.saveText != null) {
                    if (ImGui.menuItem(this.saveText.getString())) {
                        this.save();
                    }
                }

                ImGui.separator();

                ImGui.beginDisabled(immutable);
                {
                    ImGui.beginDisabled(!this.editor.canUndo());
                    if (ImGui.menuItem("Undo", "ALT-Backspace")) {
                        this.editor.undo();
                    }
                    ImGui.endDisabled();

                    ImGui.beginDisabled(!this.editor.canRedo());
                    if (ImGui.menuItem("Redo", "Ctrl-Y")) {
                        this.editor.redo();
                    }
                    ImGui.endDisabled();
                }
                ImGui.endDisabled();
                ImGui.separator();

                ImGui.beginDisabled(!this.editor.anyCursorHasSelection());
                if (ImGui.menuItem("Copy", "Ctrl-C")) {
                    this.editor.copy();
                }
                ImGui.endDisabled();

                ImGui.beginDisabled(immutable);
                {
                    ImGui.beginDisabled(!this.editor.anyCursorHasSelection());
                    if (ImGui.menuItem("Cut", "Ctrl-X")) {
                        this.editor.cut();
                    }
                    ImGui.endDisabled();

                    ImGui.beginDisabled(ImGui.getClipboardText() == null);
                    if (ImGui.menuItem("Paste", "Ctrl-V")) {
                        this.editor.paste();
                    }
                    ImGui.endDisabled();
                }
                ImGui.endDisabled();

                ImGui.endMenuBar();
            }

            TextEditorCursorPosition pos = this.editor.getCurrentCursorPosition();
            int cposX = pos.line;
            int cposY = pos.column;

            String overwrite = this.editor.isOverwriteEnabled() ? "Ovr" : "Ins";
            String canUndo = this.editor.canUndo() ? "*" : " ";

            ImGui.text(cposX + ":" + cposY + " " + this.editor.getLineCount() + " lines | " + overwrite + " | " + canUndo);

            this.editor.render("TextEditor");
        }

        ImVec2 center = ImGui.getMainViewport().getCenter();
        ImGui.setNextWindowPos(center.x, center.y, ImGuiCond.Appearing, 0.5f, 0.5f);

        if (this.saveText != null) {
            String save = this.saveText.getString();
            if (ImGui.beginPopupModal(save + "?###save_confirm", ImGuiWindowFlags.AlwaysAutoResize)) {
                ImGui.text("Your changes have not been saved.\nThis operation cannot be undone!");
                ImGui.separator();

                ImGui.setItemDefaultFocus();
                if (ImGui.button(save)) {
                    this.save();
                    this.hide();
                    ImGui.closeCurrentPopup();
                }

                ImGui.sameLine();
                if (ImGui.button("Discard")) {
                    this.oldSource = null;
                    this.hide();
                    ImGui.closeCurrentPopup();
                }

                ImGui.sameLine();
                if (ImGui.button("Cancel")) {
                    ImGui.closeCurrentPopup();
                }

                ImGui.endPopup();
            }
        }

        ImGui.popID();
    }

    public TextEditor getEditor() {
        return this.editor;
    }

    public boolean isOpen() {
        return this.open.get();
    }

    public void setSaveCallback(@Nullable SaveCallback saveCallback) {
        this.saveCallback = saveCallback;
    }

    @Override
    public void free() {
        this.editor.destroy();
    }

    /**
     * Callback for when editor is saved.
     *
     * @author Ocelot
     */
    @FunctionalInterface
    public interface SaveCallback {

        /**
         * Fired when the editor contents are saved.
         *
         * @param source The new source code
         * @param errors The error consumer
         */
        void save(String source, ErrorConsumer errors);
    }

    public interface ErrorConsumer {

        void addMarker(final int line, final int lineNumberColor, final int textColor, final String lineNumberTooltip, final String textTooltip);

        default void addMarker(final int line, final int lineNumberColor, final int textColor) {
            this.addMarker(line, lineNumberColor, textColor, "", "");
        }

        default void addMarker(final int line, final int lineNumberColor, final int textColor, final String lineNumberTooltip) {
            this.addMarker(line, lineNumberColor, textColor, lineNumberTooltip, "");
        }
    }
}
