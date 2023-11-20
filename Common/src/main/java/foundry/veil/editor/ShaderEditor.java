package foundry.veil.editor;

import foundry.veil.mixin.client.GameRendererAccessor;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.shader.program.ShaderProgram;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.texteditor.TextEditor;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40C.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL40C.GL_TESS_EVALUATION_SHADER;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;

public class ShaderEditor extends SingleWindowEditor implements ResourceManagerReloadListener {

    private static final Pattern ERROR_PARSER = Pattern.compile("ERROR: (\\d+):(\\d+): (.+)");

    private final TextEditor editor;
    private final Map<ResourceLocation, Integer> shaders;

    private final ImString programFilterText;
    private Pattern programFilter;
    private SelectedProgram selectedProgram;

    private final ImBoolean editSourceOpen;
    private int editProgramId;
    private int editShaderId;

    private String oldShaderSource;

    public ShaderEditor() {
        this.shaders = new TreeMap<>();
        this.editor = new TextEditor();
        this.editor.setShowWhitespaces(false);
        this.editor.setLanguageDefinition(TextEditorLanguageDefinition.glsl());

        this.programFilterText = new ImString(128);
        this.programFilter = null;
        this.selectedProgram = null;

        this.editSourceOpen = new ImBoolean();
        this.editProgramId = 0;
        this.editShaderId = 0;
    }

    private void setSelectedProgram(@Nullable ResourceLocation name) {
        if (name != null) {
            int program = this.shaders.get(name);
            if (glIsProgram(program)) {
                int[] attachedShaders = new int[glGetProgrami(program, GL_ATTACHED_SHADERS)];
                glGetAttachedShaders(program, null, attachedShaders);

                Map<Integer, Integer> shaders = new Int2IntArrayMap(attachedShaders.length);
                for (int shader : attachedShaders) {
                    shaders.put(glGetShaderi(shader, GL_SHADER_TYPE), shader);
                }

                this.selectedProgram = new SelectedProgram(name, program, Collections.unmodifiableMap(shaders));
                return;
            }
        }

        this.selectedProgram = null;
    }

    private void setEditShaderSource(int program, int shader) {
        this.editSourceOpen.set(true);
        this.editProgramId = program;
        this.editShaderId = shader;
        this.editor.setText(glGetShaderSource(shader));
        this.oldShaderSource = this.editor.getText();
        ImGui.setWindowFocus("###editor");
        ImGui.setWindowCollapsed("###editor", false);
    }

    private boolean hasTextChanged() {
        return !this.oldShaderSource.equals(this.editor.getText());
    }

    private Map<Integer, String> parseErrors(String log) {
        Map<Integer, String> errors = new HashMap<>();
        for (String line : log.split("\n")) {
            Matcher matcher = ERROR_PARSER.matcher(line);
            if (!matcher.find()) {
                continue;
            }

            try {
                int lineNumber = Integer.parseInt(matcher.group(2));
                if (errors.containsKey(lineNumber)) {
                    continue;
                }

                String error = matcher.group(3);
                errors.put(lineNumber, error);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return errors;
    }

    private void saveShaderSource() {
        if (this.selectedProgram == null || !glIsShader(this.editShaderId) || !this.hasTextChanged()) {
            return;
        }

        glShaderSource(this.editShaderId, this.editor.getText());
        glCompileShader(this.editShaderId);
        if (glGetShaderi(this.editShaderId, GL_COMPILE_STATUS) != GL_TRUE) {
            String log = glGetShaderInfoLog(this.editShaderId);
            this.editor.setErrorMarkers(this.parseErrors(log));
            System.out.println(log);
            return;
        }

        glLinkProgram(this.editProgramId);
        if (glGetProgrami(this.editProgramId, GL_LINK_STATUS) != GL_TRUE) {
            String log = glGetProgramInfoLog(this.editProgramId);
            this.editor.setErrorMarkers(this.parseErrors(log));
            System.out.println(log);
            return;
        }

        this.oldShaderSource = this.editor.getText();
        this.editor.setErrorMarkers(Collections.emptyMap());
    }

    private void reloadShaders() {
        this.shaders.clear();

        GameRendererAccessor gameRendererAccessor = (GameRendererAccessor) Minecraft.getInstance().gameRenderer;
        for (ShaderInstance shader : gameRendererAccessor.getShaders().values()) {
            String name = shader.getName().isBlank() ? Integer.toString(shader.getId()) : shader.getName();
            this.shaders.put(new ResourceLocation(name), shader.getId());
        }

        for (ShaderProgram shader : VeilRenderSystem.renderer().getShaderManager().getShaders().values()) {
            this.shaders.put(shader.getId(), shader.getProgram());
        }

        if (this.selectedProgram != null && !this.shaders.containsKey(this.selectedProgram.name)) {
            this.setSelectedProgram(null);
        }
    }

    @Override
    public String getDisplayName() {
        return "Shaders";
    }

    @Override
    protected void renderComponents() {
        ImGui.beginGroup();
        ImGui.text("Shader Programs");

        if (ImGui.button("Refresh")) {
            this.reloadShaders();
        }

        if (ImGui.inputTextWithHint("##search", "Search...", this.programFilterText)) {
            String regex = this.programFilterText.get();
            this.programFilter = null;
            if (!regex.isBlank()) {
                try {
                    this.programFilter = Pattern.compile(regex);
                } catch (PatternSyntaxException ignored) {
                }
            }
        }

        if (ImGui.beginListBox("##programs", 0, -Float.MIN_VALUE)) {
            for (ResourceLocation name : this.shaders.keySet()) {
                boolean selected = this.selectedProgram != null && name.equals(this.selectedProgram.name);

                if (this.programFilter != null && !this.programFilter.matcher(name.toString()).find()) {
                    if (selected) {
                        this.setSelectedProgram(null);
                    }
                    continue;
                }

                if (ImGui.selectable(name.toString(), selected)) {
                    this.setSelectedProgram(name);
                }
            }

            ImGui.endListBox();
        }
        ImGui.endGroup();

        ImGui.sameLine();
        ImGui.beginGroup();
        ImGui.text("Open Source");

        this.openShaderButton("Vertex Shader", GL_VERTEX_SHADER);
        this.openShaderButton("Tesselation Control Shader", GL_TESS_CONTROL_SHADER);
        this.openShaderButton("Tesselation Evaluation Shader", GL_TESS_EVALUATION_SHADER);
        this.openShaderButton("Geometry Shader", GL_GEOMETRY_SHADER);
        this.openShaderButton("Fragment Shader", GL_FRAGMENT_SHADER);
        this.openShaderButton("Compute Shader", GL_COMPUTE_SHADER);

        ImGui.endGroup();

        if (this.oldShaderSource == null) {
            return;
        }

        int flags = ImGuiWindowFlags.MenuBar;
        if (this.hasTextChanged()) {
            flags |= ImGuiWindowFlags.UnsavedDocument;
        } else {
            this.editor.setErrorMarkers(Collections.emptyMap());
        }
        boolean wasOpen = this.editSourceOpen.get();

        ImGui.setNextWindowSizeConstraints(800, 600, Float.MAX_VALUE, Float.MAX_VALUE);
        if (ImGui.begin("Edit Shader###editor", this.editSourceOpen, flags)) {
            if (ImGui.beginMenuBar()) {
                boolean immutable = this.editor.isReadOnly();
                if (ImGui.menuItem("Read-only mode", "", immutable)) {
                    this.editor.setReadOnly(!immutable);
                }
                if (ImGui.menuItem("Show Whitespace", "", this.editor.isShowingWhitespaces())) {
                    this.editor.setShowWhitespaces(!this.editor.isShowingWhitespaces());
                }

                ImGui.beginDisabled(!this.hasTextChanged());
                if (ImGui.menuItem("Upload")) {
                    this.saveShaderSource();
                }
                ImGui.endDisabled();

                ImGui.separator();

                ImGui.beginDisabled(immutable);
                {
                    ImGui.beginDisabled(!this.editor.canUndo());
                    if (ImGui.menuItem("Undo", "ALT-Backspace")) {
                        this.editor.undo(1);
                    }
                    ImGui.endDisabled();

                    ImGui.beginDisabled(!this.editor.canRedo());
                    if (ImGui.menuItem("Redo", "Ctrl-Y")) {
                        this.editor.redo(1);
                    }
                    ImGui.endDisabled();
                }
                ImGui.endDisabled();
                ImGui.separator();

                ImGui.beginDisabled(!this.editor.hasSelection());
                if (ImGui.menuItem("Copy", "Ctrl-C")) {
                    this.editor.copy();
                }
                ImGui.endDisabled();

                ImGui.beginDisabled(immutable);
                {
                    ImGui.beginDisabled(!this.editor.hasSelection());
                    if (ImGui.menuItem("Cut", "Ctrl-X")) {
                        this.editor.cut();
                    }
                    if (ImGui.menuItem("Delete", "Del")) {
                        this.editor.delete();
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

            int cposX = this.editor.getCursorPositionLine();
            int cposY = this.editor.getCursorPositionColumn();

            String overwrite = this.editor.isOverwrite() ? "Ovr" : "Ins";
            String canUndo = this.editor.canUndo() ? "*" : " ";

            ImGui.text(cposX + ":" + cposY + " " + this.editor.getTotalLines() + " lines | " + overwrite + " | " + canUndo);

            this.editor.render("TextEditor");
        }
        ImGui.end();

        if (!this.editSourceOpen.get() && wasOpen) {
            if (this.hasTextChanged()) {
                this.editSourceOpen.set(true);
                ImGui.openPopup("Upload?");
            } else {
                this.oldShaderSource = null;
            }
        }

        ImVec2 center = ImGui.getMainViewport().getCenter();
        ImGui.setNextWindowPos(center.x, center.y, ImGuiCond.Appearing, 0.5f, 0.5f);

        if (ImGui.beginPopupModal("Upload?", ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text("Your changes have not been saved.\nThis operation cannot be undone!");
            ImGui.separator();

            ImGui.setItemDefaultFocus();
            if (ImGui.button("Upload")) {
                this.saveShaderSource();
                this.editSourceOpen.set(false);
                this.oldShaderSource = null;
                ImGui.closeCurrentPopup();
            }

            ImGui.sameLine();
            if (ImGui.button("Discard")) {
                this.editSourceOpen.set(false);
                this.oldShaderSource = null;
                ImGui.closeCurrentPopup();
            }

            ImGui.sameLine();
            if (ImGui.button("Cancel")) {
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    private void openShaderButton(String name, int type) {
        ImGui.beginDisabled(this.selectedProgram == null || !this.selectedProgram.shaders.containsKey(type));
        if (ImGui.button(name)) {
            this.setEditShaderSource(this.selectedProgram.programId, this.selectedProgram.shaders.get(type));
        }
        ImGui.endDisabled();
    }

    @Override
    public void onShow() {
        super.onShow();
        this.reloadShaders();
    }

    @Override
    public void onHide() {
        super.onHide();
        this.shaders.clear();
    }

    @Override
    public void free() {
        super.free();
        this.editor.destroy();
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        if (this.isOpen()) {
            this.reloadShaders();
        }
    }

    private record SelectedProgram(ResourceLocation name, int programId, Map<Integer, Integer> shaders) {
    }
}
