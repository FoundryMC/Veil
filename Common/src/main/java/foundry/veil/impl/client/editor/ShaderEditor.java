package foundry.veil.impl.client.editor;

import foundry.veil.Veil;
import foundry.veil.api.client.editor.SingleWindowEditor;
import foundry.veil.api.client.imgui.CodeEditor;
import foundry.veil.api.client.imgui.VeilLanguageDefinitions;
import foundry.veil.mixin.client.shader.GameRendererAccessor;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.shader.definition.ShaderPreDefinitions;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40C.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL40C.GL_TESS_EVALUATION_SHADER;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;

@ApiStatus.Internal
public class ShaderEditor extends SingleWindowEditor implements ResourceManagerReloadListener {

    private static final Pattern ERROR_PARSER = Pattern.compile("ERROR: (\\d+):(\\d+): (.+)");
    private static final Pattern LINE_DIRECTIVE_PARSER = Pattern.compile("#line\\s+(\\d+)\\s*(\\d+)?");

    private final CodeEditor codeEditor;
    private final Map<ResourceLocation, Integer> shaders;

    private final ImString programFilterText;
    private Pattern programFilter;
    private SelectedProgram selectedProgram;
    private int selectedTab;

    private final ImString addDefinitionText;
    private final Set<String> removedDefinitions;

    private final ImBoolean editSourceOpen;
    private int editProgramId;
    private int editShaderId;

    public ShaderEditor() {
        this.shaders = new TreeMap<>();

        this.codeEditor = new CodeEditor("Upload");
        this.codeEditor.setSaveCallback((source, errorConsumer) -> {
            if (this.selectedProgram == null || !glIsShader(this.editShaderId)) {
                errorConsumer.accept(0, "Invalid Shader");
                return;
            }

            glShaderSource(this.editShaderId, source);
            glCompileShader(this.editShaderId);
            if (glGetShaderi(this.editShaderId, GL_COMPILE_STATUS) != GL_TRUE) {
                String log = glGetShaderInfoLog(this.editShaderId);
                parseErrors(source, log).forEach(errorConsumer);
                System.out.println(log);
                return;
            }

            glLinkProgram(this.editProgramId);
            if (glGetProgrami(this.editProgramId, GL_LINK_STATUS) != GL_TRUE) {
                String log = glGetProgramInfoLog(this.editProgramId);
                parseErrors(source, log).forEach(errorConsumer);
                System.out.println(log);
            }
        });
        this.codeEditor.getEditor().setLanguageDefinition(VeilLanguageDefinitions.glsl());

        this.programFilterText = new ImString(128);
        this.programFilter = null;
        this.selectedProgram = null;
        this.selectedTab = 0;

        this.addDefinitionText = new ImString(128);
        this.removedDefinitions = new HashSet<>(1);

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
            } else {
                System.out.println("Compiled shader does not exist for selected program.");
            }
        }

        this.selectedProgram = null;
    }

    private void setEditShaderSource(int program, int shader) {
        this.editSourceOpen.set(true);
        this.editProgramId = program;
        this.editShaderId = shader;
        this.codeEditor.show(glGetShaderSource(shader));
    }

    private static Map<Integer, String> parseErrors(String source, String log) {
        Map<Integer, Map<Integer, String>> logErrors = parseLogErrors(log);

        Map<Integer, String> foundErrors = new HashMap<>();
        int sourceId = 0;
        int lineNumber = 0;
        int sourceLineNumber = -1;
        for (String line : source.split("\n")) {
            sourceLineNumber++;

            Matcher matcher = LINE_DIRECTIVE_PARSER.matcher(line);
            if (matcher.find()) {
                try {
                    lineNumber = Integer.parseInt(matcher.group(1));
                    if (matcher.groupCount() > 1) {
                        sourceId = Integer.parseInt(matcher.group(2));
                    }
                } catch (Throwable ignored) {
                }
                continue;
            }

            Map<Integer, String> errors = logErrors.get(sourceId);
            if (errors != null) {
                String error = errors.remove(lineNumber);
                if (error != null) {
                    foundErrors.put(sourceLineNumber, error);
                }
            }

            lineNumber++;
        }

        return foundErrors;
    }

    private static Map<Integer, Map<Integer, String>> parseLogErrors(String log) {
        Map<Integer, Map<Integer, String>> logErrors = new HashMap<>();
        for (String line : log.split("\n")) {
            Matcher matcher = ERROR_PARSER.matcher(line);
            if (!matcher.find()) {
                continue;
            }

            try {
                int sourceNumber = Integer.parseInt(matcher.group(1));
                int lineNumber = Integer.parseInt(matcher.group(2));

                Map<Integer, String> errors = logErrors.computeIfAbsent(sourceNumber, unused -> new HashMap<>());
                if (errors.containsKey(lineNumber)) {
                    continue;
                }

                String error = matcher.group(3);
                errors.put(lineNumber, error);
            } catch (Throwable ignored) {
            }
        }
        return logErrors;
    }

    private void reloadShaders() {
        this.shaders.clear();
        GameRendererAccessor gameRendererAccessor = (GameRendererAccessor) Minecraft.getInstance().gameRenderer;
        VeilRenderer veilRenderer = VeilRenderSystem.renderer();

        switch (TabSource.values()[this.selectedTab]) {
            case VANILLA -> {
                for (ShaderInstance shader : gameRendererAccessor.getShaders().values()) {
                    String name = shader.getName().isBlank() ? Integer.toString(shader.getId()) : shader.getName();
                    this.shaders.put(new ResourceLocation(name), shader.getId());
                }
            }
            case VEIL -> {
                for (ShaderProgram shader : veilRenderer.getShaderManager().getShaders().values()) {
                    this.shaders.put(shader.getId(), shader.getProgram());
                }
            }
            case VEIL_DEFERRED -> {
                for (ShaderProgram shader : veilRenderer.getDeferredRenderer().getDeferredShaderManager().getShaders().values()) {
                    this.shaders.put(shader.getId(), shader.getProgram());
                }
            }
            case OTHER -> {
                for (int i = 0; i < 10000; i++) {
                    if (!glIsProgram(i)) {
                        continue;
                    }

                    this.shaders.put(new ResourceLocation("unknown", Integer.toString(i)), i);
                }

                for (ShaderProgram shader : veilRenderer.getShaderManager().getShaders().values()) {
                    this.shaders.values().remove(shader.getProgram());
                }
                for (ShaderInstance shader : gameRendererAccessor.getShaders().values()) {
                    this.shaders.values().remove(shader.getId());
                }
            }
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
        this.removedDefinitions.clear();

        ImGui.beginChild("Shader Programs", ImGui.getContentRegionAvailX() * 2 / 3, 0);
        ImGui.text("Shader Programs");

        TabSource[] sources = TabSource.values();
        if (ImGui.beginTabBar("##controls")) {
            if (ImGui.tabItemButton("Refresh")) {
                this.reloadShaders();
            }
            for (TabSource source : sources) {
                ImGui.beginDisabled(!source.active.getAsBoolean());
                if (ImGui.beginTabItem(source.displayName)) {
                    if (this.selectedTab != source.ordinal()) {
                        this.selectedTab = source.ordinal();
                        this.setSelectedProgram(null);
                        this.reloadShaders();
                    }
                    ImGui.endTabItem();
                }
                ImGui.endDisabled();
            }
            ImGui.endTabBar();
        }

        // Deselect the tab if it is no longer active
        while (!sources[this.selectedTab].active.getAsBoolean() && this.selectedTab > 0) {
            this.selectedTab--;
            this.setSelectedProgram(null);
            this.reloadShaders();
        }

        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
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

        if (ImGui.beginListBox("##programs", ImGui.getContentRegionAvailX(), -Float.MIN_VALUE)) {
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
        ImGui.endChild();

        ShaderPreDefinitions definitions = VeilRenderSystem.renderer().getShaderDefinitions();
        ImGui.sameLine();
        if (ImGui.beginChild("Panel", 0, ImGui.getContentRegionAvailY())) {
            if (ImGui.beginChild("Open Source", 0, ImGui.getContentRegionAvailY() / 2)) {
                ImGui.text("Open Source");

                this.openShaderButton("Vertex Shader", GL_VERTEX_SHADER);
                this.openShaderButton("Tesselation Control Shader", GL_TESS_CONTROL_SHADER);
                this.openShaderButton("Tesselation Evaluation Shader", GL_TESS_EVALUATION_SHADER);
                this.openShaderButton("Geometry Shader", GL_GEOMETRY_SHADER);
                this.openShaderButton("Fragment Shader", GL_FRAGMENT_SHADER);
                this.openShaderButton("Compute Shader", GL_COMPUTE_SHADER);
            }
            ImGui.endChild();

            if (ImGui.beginChild("Shader Definitions", 0, ImGui.getContentRegionAvailY())) {
                ImGui.text("Shader Definitions:");
                ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
                if (ImGui.inputTextWithHint("##add_definition", "name = value", this.addDefinitionText, ImGuiInputTextFlags.EnterReturnsTrue)) {
                    definitions.define(this.addDefinitionText.get().trim());
                    this.addDefinitionText.clear();
                }
                if (ImGui.beginListBox("##definitions", -Float.MIN_VALUE, ImGui.getContentRegionAvailY())) {
                    for (Map.Entry<String, String> entry : definitions.getDefinitions().entrySet()) {
                        String name = entry.getKey();
                        String value = entry.getValue();

                        ImGui.pushID(name);
                        ImGui.text(value);

                        float size = ImGui.getTextLineHeightWithSpacing();
                        ImGui.sameLine();
                        ImGui.dummy(ImGui.getContentRegionAvailX() - ImGui.getStyle().getCellPaddingX() * 2 - size, 0);
                        ImGui.sameLine();
                        if (ImGui.button("X", size, size)) {
                            this.removedDefinitions.add(name);
                        }

                        ImGui.popID();
                    }

                    ImGui.endListBox();
                }
            }
            ImGui.endChild();
        }
        ImGui.endChild();

        for (String name : this.removedDefinitions) {
            definitions.remove(name);
        }
    }

    @Override
    public void render() {
        ImGui.setNextWindowSizeConstraints(600, 400, Float.MAX_VALUE, Float.MAX_VALUE);

        super.render();

        this.codeEditor.renderWindow();
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
        this.codeEditor.free();
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        if (this.isOpen()) {
            this.reloadShaders();
        }
    }

    private record SelectedProgram(ResourceLocation name, int programId, Map<Integer, Integer> shaders) {
    }

    private enum TabSource {
        VANILLA("Vanilla"),
        VEIL("Veil"),
        VEIL_DEFERRED("Veil Deferred", () -> VeilRenderSystem.renderer().getDeferredRenderer().isEnabled()),
        OTHER("Unknown");

        private final String displayName;
        private final BooleanSupplier active;

        TabSource(String displayName) {
            this(displayName, () -> true);
        }

        TabSource(String displayName, BooleanSupplier active) {
            this.displayName = displayName;
            this.active = active;
        }
    }
}
