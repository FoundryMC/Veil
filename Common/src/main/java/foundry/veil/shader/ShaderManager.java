package foundry.veil.shader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import foundry.veil.framebuffer.FramebufferManager;
import foundry.veil.pipeline.VeilRenderSystem;
import foundry.veil.pipeline.VeilRenderer;
import foundry.veil.post.PostProcessingManager;
import foundry.veil.shader.compiler.ShaderCompiler;
import foundry.veil.shader.definition.ShaderPreDefinitions;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40C.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL40C.GL_TESS_EVALUATION_SHADER;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;

/**
 * <p>Manages all shaders and compiles them automatically.</p>
 * <p>Shaders can be recompiled using {@link #recompile(ResourceLocation, ResourceProvider)} or
 * {@link #recompile(ResourceLocation, ResourceProvider, ShaderCompiler)} to use a custom compiler.</p>
 *
 * @author Ocelot
 * @see ShaderCompiler
 */
public class ShaderManager implements PreparableReloadListener, Closeable {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(ProgramDefinition.class, new ProgramDefinition.Deserializer())
            .create();

    public static final String FOLDER = "pinwheel/shaders/program";
    public static final FileToIdConverter INCLUDE_LISTER = new FileToIdConverter("pinwheel/shaders/include", ".glsl");
    public static final FileToIdConverter SHADER_LISTER = FileToIdConverter.json(FOLDER);
    public static final FileToIdConverter CORE_SHADER_LISTER = FileToIdConverter.json(FOLDER + "/core");
    public static final FileToIdConverter GLSL_CONVERTER = new FileToIdConverter(FOLDER, ".glsl");

    private static final Map<Integer, String> TYPES = Map.of(
            GL_VERTEX_SHADER, "vertex",
            GL_TESS_CONTROL_SHADER, "tesselation_control",
            GL_TESS_EVALUATION_SHADER, "tesselation_evaluation",
            GL_GEOMETRY_SHADER, "geometry",
            GL_FRAGMENT_SHADER, "fragment",
            GL_COMPUTE_SHADER, "compute"
    );
    private static final Map<Integer, FileToIdConverter> EXTENSIONS = Map.of(
            GL_VERTEX_SHADER, new FileToIdConverter(FOLDER, ".vsh"),
            GL_TESS_CONTROL_SHADER, new FileToIdConverter(FOLDER, ".tcsh"),
            GL_TESS_EVALUATION_SHADER, new FileToIdConverter(FOLDER, ".tesh"),
            GL_GEOMETRY_SHADER, new FileToIdConverter(FOLDER, ".gsh"),
            GL_FRAGMENT_SHADER, new FileToIdConverter(FOLDER, ".fsh"),
            GL_COMPUTE_SHADER, new FileToIdConverter(FOLDER, ".csh")
    );

    private final ShaderPreDefinitions definitions;
    private final Map<ResourceLocation, ShaderProgram> shaders;
    private final Map<ResourceLocation, ShaderProgram> coreShaders;
    private final Set<ResourceLocation> dirtyShaders;
    private CompletableFuture<Void> reloadFuture;
    private CompletableFuture<Void> recompileFuture;

    /**
     * Creates a new shader manager.
     */
    public ShaderManager() {
        this.definitions = new ShaderPreDefinitions(this::onDefinitionChanged);
        this.shaders = new HashMap<>();
        this.coreShaders = new HashMap<>();
        this.dirtyShaders = new HashSet<>();
        this.reloadFuture = CompletableFuture.completedFuture(null);
        this.recompileFuture = CompletableFuture.completedFuture(null);
    }

    private void onDefinitionChanged(String definition) {
        Stream.concat(this.coreShaders.values().stream(), this.shaders.values().stream()).forEach(shader -> {
            if (shader.getDefinitionDependencies().contains(definition)) {
                LOGGER.debug("{} changed, recompiling {}", definition, shader.getId());
                this.scheduleRecompile(shader.getId());
            }
        });
    }

    private ProgramDefinition parseDefinition(ResourceLocation id, ResourceProvider provider) throws IOException {
        try (Reader reader = provider.openAsReader(SHADER_LISTER.idToFile(id))) {
            ProgramDefinition definition = GsonHelper.fromJson(GSON, reader, ProgramDefinition.class);
            if (definition.vertex() == null &&
                    definition.tesselationControl() == null &&
                    definition.tesselationEvaluation() == null &&
                    definition.geometry() == null &&
                    definition.fragment() == null &&
                    definition.compute() == null) {
                throw new JsonSyntaxException("Shader programs must define at least 1 shader type");
            }

            return definition;
        } catch (JsonParseException e) {
            throw new IOException(e);
        }
    }

    private void readShader(ResourceManager resourceManager, Map<ResourceLocation, ProgramDefinition> definitions, Map<ResourceLocation, Resource> shaderSources, ResourceLocation key, boolean allowCore) {
        Set<ResourceLocation> checkedSources = new HashSet<>();
        ResourceLocation id = SHADER_LISTER.fileToId(key);

        if (!allowCore && this.coreShaders.containsKey(id)) {
            return;
        }

        try {
            ProgramDefinition definition = this.parseDefinition(id, resourceManager);
            if (definitions.put(id, definition) != null) {
                throw new IllegalStateException("Duplicate shader ignored with ID " + id);
            }

            for (Map.Entry<Integer, ResourceLocation> shader : definition.shaders().entrySet()) {
                FileToIdConverter converter = ShaderManager.getTypeConverter(shader.getKey());
                ResourceLocation location = converter.idToFile(shader.getValue());

                if (!checkedSources.add(location)) {
                    continue;
                }

                Resource resource = resourceManager.getResourceOrThrow(location);
                try (InputStream stream = resource.open()) {
                    byte[] source = stream.readAllBytes();
                    Resource fileResource =
                            new Resource(resource.source(), () -> new ByteArrayInputStream(source));
                    shaderSources.put(location, fileResource);
                }
            }
        } catch (IOException | IllegalArgumentException | JsonParseException e) {
            LOGGER.error("Couldn't parse shader {} from {}", id, key, e);
        }
    }

    private Map<ResourceLocation, Resource> readIncludes(ResourceManager resourceManager) {
        Map<ResourceLocation, Resource> shaderSources = new HashMap<>();
        Set<ResourceLocation> checkedSources = new HashSet<>();

        for (Map.Entry<ResourceLocation, Resource> entry :
                INCLUDE_LISTER.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation location = entry.getKey();
            ResourceLocation id = INCLUDE_LISTER.fileToId(location);

            if (!checkedSources.add(location)) {
                continue;
            }

            try {
                Resource resource = resourceManager.getResourceOrThrow(location);
                try (InputStream stream = resource.open()) {
                    byte[] source = stream.readAllBytes();
                    Resource fileResource = new Resource(resource.source(), () -> new ByteArrayInputStream(source));
                    shaderSources.put(location, fileResource);
                }
            } catch (IOException | IllegalArgumentException | JsonParseException e) {
                LOGGER.error("Couldn't parse shader import {} from {}", id, location, e);
            }
        }

        return shaderSources;
    }

    private void compile(ShaderProgram program, ProgramDefinition definition, ShaderCompiler compiler) {
        ResourceLocation id = program.getId();
        try {
            program.compile(new ShaderCompiler.Context(this.definitions, definition), compiler);
        } catch (ShaderException e) {
            LOGGER.error("Failed to create shader {}: {}", id, e.getMessage());
            LOGGER.warn(e.getGlError());
        } catch (Exception e) {
            LOGGER.error("Failed to create shader: {}", id, e);
        }
    }

    private ShaderProgram preload(ResourceProvider resourceProvider, ShaderCompiler compiler, ResourceLocation name) {
        try {
            ProgramDefinition definition = this.parseDefinition(name, resourceProvider);
            ShaderProgram shader = new ShaderProgram(name);
            this.compile(shader, definition, compiler);
            return shader;
        } catch (Exception exception) {
            throw new IllegalStateException("could not preload shader " + name, exception);
        }
    }

    /**
     * Preloads the UI shader on the main thread once.
     *
     * @param resourceProvider The provider for resources
     */
    public void preloadUi(ResourceManager resourceProvider) {
        if (!this.coreShaders.isEmpty()) {
            throw new RuntimeException("Core shaders already preloaded");
        }

        try (ShaderCompiler compiler = ShaderCompiler.cached(resourceProvider).addDefaultProcessors()) {
            Map<ResourceLocation, Resource> resources = CORE_SHADER_LISTER.listMatchingResources(resourceProvider);
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                ResourceLocation key = entry.getKey();
                ResourceLocation id = SHADER_LISTER.fileToId(key);
                this.coreShaders.put(id, this.preload(resourceProvider, compiler, id));
            }
        }
    }

    /**
     * Attempts to recompile the shader with the specified id.
     *
     * @param id       The id of the shader to recompile
     * @param provider The source of resources
     */
    public void recompile(ResourceLocation id, ResourceProvider provider) {
        try (ShaderCompiler compiler = ShaderCompiler.direct(provider).addDefaultProcessors()) {
            this.recompile(id, provider, compiler);
        }
    }

    /**
     * Attempts to recompile the shader with the specified id.
     *
     * @param id       The id of the shader to recompile
     * @param provider The source of resources
     * @param compiler The compiler instance to use. If unsure, use {@link #recompile(ResourceLocation, ResourceProvider)}
     */
    public void recompile(ResourceLocation id, ResourceProvider provider, ShaderCompiler compiler) {
        ShaderProgram program = this.shaders.get(id);
        if (program == null) {
            LOGGER.error("Failed to recompile unknown shader: {}", id);
            return;
        }

        if (this.coreShaders.containsKey(id)) {
            LOGGER.error("Core shaders cannot be recompiled");
            return;
        }

        try {
            this.compile(program, this.parseDefinition(id, provider), compiler);
        } catch (Exception e) {
            LOGGER.error("Failed to read shader definition: {}", id, e);
        }
    }

    /**
     * Sets a global shader value.
     *
     * @param setter The setter for shaders
     */
    public void setGlobal(Consumer<ShaderProgram> setter) {
        this.shaders.values().forEach(setter);
    }

    /**
     * @return The manager for all pre-definitions
     */
    public ShaderPreDefinitions getDefinitions() {
        return this.definitions;
    }

    /**
     * Retrieves a shader by the specified id.
     *
     * @param id The id of the shader to retrieve
     * @return The retrieved shader or <code>null</code> if there is no valid shader with that id
     */
    public @Nullable ShaderProgram getShader(ResourceLocation id) {
        if (this.coreShaders.containsKey(id)) {
            return this.coreShaders.get(id);
        }
        return this.shaders.get(id);
    }

    private ReloadState prepare(ResourceManager resourceManager) {
        Map<ResourceLocation, ProgramDefinition> definitions = new HashMap<>();
        Map<ResourceLocation, Resource> shaderSources = new HashMap<>();

        for (Map.Entry<ResourceLocation, Resource> entry :
                SHADER_LISTER.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation key = entry.getKey();
            this.readShader(resourceManager, definitions, shaderSources, key, false);
        }
        shaderSources.putAll(this.readIncludes(resourceManager));

        return new ReloadState(definitions, shaderSources);
    }

    private void apply(ShaderManager.ReloadState reloadState) {
        this.shaders.values().forEach(ShaderProgram::free);
        this.shaders.clear();

        Map<ResourceLocation, Resource> shaderSources = reloadState.shaderSources();
        ResourceProvider sourceProvider = loc -> Optional.ofNullable(shaderSources.get(loc));
        try (ShaderCompiler compiler = ShaderCompiler.cached(sourceProvider).addDefaultProcessors()) {
            for (Map.Entry<ResourceLocation, ProgramDefinition> entry : reloadState.definitions().entrySet()) {
                ResourceLocation id = entry.getKey();
                ShaderProgram program = new ShaderProgram(id);
                this.compile(program, entry.getValue(), compiler);
                this.shaders.put(id, program);
            }
        }

        VeilRenderSystem.finalizeShaderCompilation();

        LOGGER.info("Loaded {} shaders", this.shaders.size());
    }

    private ReloadState prepareRecompile(ResourceManager resourceManager, Set<ResourceLocation> shaders) {
        Map<ResourceLocation, ProgramDefinition> definitions = new HashMap<>();
        Map<ResourceLocation, Resource> shaderSources = new HashMap<>();

        for (ResourceLocation key : shaders) {
            this.readShader(resourceManager, definitions, shaderSources, SHADER_LISTER.idToFile(key), true);
        }
        shaderSources.putAll(this.readIncludes(resourceManager));

        return new ReloadState(definitions, shaderSources);
    }

    private void applyRecompile(ShaderManager.ReloadState reloadState, Set<ResourceLocation> shaders) {
        Map<ResourceLocation, Resource> shaderSources = reloadState.shaderSources();
        ResourceProvider sourceProvider = loc -> Optional.ofNullable(shaderSources.get(loc));
        try (ShaderCompiler compiler = ShaderCompiler.cached(sourceProvider).addDefaultProcessors()) {
            for (Map.Entry<ResourceLocation, ProgramDefinition> entry : reloadState.definitions().entrySet()) {
                ResourceLocation id = entry.getKey();
                ShaderProgram program = this.getShader(id);
                if (program == null) {
                    LOGGER.warn("Failed to recompile shader: {}", id);
                    continue;
                }
                this.compile(program, entry.getValue(), compiler);
            }
        }

        VeilRenderSystem.finalizeShaderCompilation();

        LOGGER.info("Recompiled {} shaders", shaders.size());
    }

    private void scheduleRecompile() {
        Minecraft client = Minecraft.getInstance();
        client.tell(() -> {
            if (!this.recompileFuture.isDone()) {
                return;
            }

            Set<ResourceLocation> shaders;
            synchronized (this.dirtyShaders) {
                shaders = new HashSet<>(this.dirtyShaders);
                this.dirtyShaders.clear();
            }
            this.recompileFuture = CompletableFuture.supplyAsync(() -> this.prepareRecompile(client.getResourceManager(), shaders), Util.backgroundExecutor())
                    .thenAcceptAsync(state -> this.applyRecompile(state, shaders), client)
                    .handle((value, e) -> {
                        if (e != null) {
                            LOGGER.error("Error recompiling shaders", e);
                        }

                        synchronized (this.dirtyShaders) {
                            if (this.dirtyShaders.isEmpty()) {
                                return value;
                            }
                        }

                        this.scheduleRecompile();
                        return value;
                    });
        });
    }

    /**
     * Schedules a shader recompilation on the next loop iteration.
     *
     * @param shader The shader to recompile
     */
    public void scheduleRecompile(ResourceLocation shader) {
        synchronized (this.dirtyShaders) {
            this.dirtyShaders.add(shader);
        }

        if (!this.recompileFuture.isDone()) {
            return;
        }

        this.scheduleRecompile();
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(@NotNull PreparationBarrier preparationBarrier, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller prepareProfiler, @NotNull ProfilerFiller applyProfiler, @NotNull Executor backgroundExecutor, @NotNull Executor gameExecutor) {
        if (this.reloadFuture != null && !this.reloadFuture.isDone()) {
            return this.reloadFuture.thenCompose(preparationBarrier::wait);
        }
        return this.reloadFuture = this.recompileFuture.thenCompose(
                unused -> CompletableFuture.supplyAsync(() -> this.prepare(resourceManager), backgroundExecutor)
                        .thenCompose(preparationBarrier::wait)
                        .thenAcceptAsync(this::apply, gameExecutor));
    }

    /**
     * Recompiles all shaders in the background.
     *
     * @param resourceManager    The manager for resources. Shader files and definitions are loaded from this manager
     * @param backgroundExecutor The executor for preparation tasks
     * @param gameExecutor       The executor for applying the shaders
     * @return A future representing when shader compilation will be done
     */
    public CompletableFuture<Void> reload(ResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor) {
        VeilRenderer renderer = VeilRenderSystem.getRenderer();
        FramebufferManager framebufferManager = renderer.getFramebufferManager();
        PostProcessingManager postProcessingManager = renderer.getPostProcessingManager();

        return this.reloadFuture = CompletableFuture.allOf(
                this.reload(this, resourceManager, backgroundExecutor, gameExecutor),
                this.reload(framebufferManager, resourceManager, backgroundExecutor, gameExecutor),
                this.reload(postProcessingManager, resourceManager, backgroundExecutor, gameExecutor));
    }

    private CompletableFuture<Void> reload(PreparableReloadListener listener, ResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor) {
        return listener.reload(CompletableFuture::completedFuture,
                resourceManager,
                InactiveProfiler.INSTANCE,
                InactiveProfiler.INSTANCE,
                backgroundExecutor,
                gameExecutor);
    }

    /**
     * @return The current future for full shader reload status
     */
    public CompletableFuture<Void> getReloadFuture() {
        return this.reloadFuture;
    }

    /**
     * @return The current future for dirty shader recompilation status
     */
    public CompletableFuture<Void> getRecompileFuture() {
        return this.recompileFuture;
    }

    /**
     * Retrieves a readable name for a shader type. Supports all shader types instead of just vertex and fragment.
     *
     * @param type The GL enum for the type
     * @return The readable name or a hex value if the type is unknown
     */
    public static String getTypeName(int type) {
        String value = TYPES.get(type);
        return value != null ? value : "0x" + Integer.toHexString(type);
    }

    /**
     * Retrieves the id converter of a shader type.
     *
     * @param type The GL enum for the type
     * @return The file type converter or <code>glsl</code> if the type is unknown
     */
    public static FileToIdConverter getTypeConverter(int type) {
        FileToIdConverter value = EXTENSIONS.get(type);
        return value != null ? value : GLSL_CONVERTER;
    }

    @Override
    public void close() {
        this.shaders.values().forEach(ShaderProgram::free);
        this.shaders.clear();
        this.coreShaders.values().forEach(ShaderProgram::free);
        this.coreShaders.clear();
    }

    record ReloadState(Map<ResourceLocation, ProgramDefinition> definitions,
                       Map<ResourceLocation, Resource> shaderSources) {
    }
}
