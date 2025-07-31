package foundry.veil.impl.client.render.shader.program;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.ext.VeilDebug;
import foundry.veil.api.client.render.shader.ShaderFeature;
import foundry.veil.api.client.render.shader.ShaderSourceSet;
import foundry.veil.api.client.render.shader.block.ShaderBlock;
import foundry.veil.api.client.render.shader.compiler.CompiledShader;
import foundry.veil.api.client.render.shader.compiler.ShaderCompiler;
import foundry.veil.api.client.render.shader.compiler.ShaderException;
import foundry.veil.api.client.render.shader.compiler.VeilShaderSource;
import foundry.veil.api.client.render.shader.program.ProgramDefinition;
import foundry.veil.api.client.render.shader.program.ShaderBlendMode;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.program.ShaderUniformCache;
import foundry.veil.api.client.render.shader.texture.ShaderTextureSource;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.api.client.render.texture.SamplerObject;
import foundry.veil.api.client.render.texture.TextureFilter;
import foundry.veil.api.client.util.VertexFormatCodec;
import foundry.veil.impl.client.render.shader.uniform.ShaderUniformImpl;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL31C.GL_INVALID_INDEX;
import static org.lwjgl.opengl.GL42C.GL_UNSIGNED_INT_ATOMIC_COUNTER;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;
import static org.lwjgl.opengl.KHRDebug.GL_PROGRAM;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class ShaderProgramImpl implements ShaderProgram {

    public static final VeilShaderSource DUMMY_FRAGMENT_SHADER = new VeilShaderSource(null, "out vec4 fragColor;void main(){fragColor=vec4(1.0);}");
    private static final Matrix4f MODEL_VIEW_MATRIX = new Matrix4f();
    private static final Matrix4f PROJECTION_MATRIX = new Matrix4f();

    private static boolean restoreEquation = false;
    private static int srcColorFactor = -1;
    private static int dstColorFactor = -1;
    private static int srcAlphaFactor = -1;
    private static int dstAlphaFactor = -1;

    private final ResourceLocation name;
    private final ShaderTextureCache textures;
    private final Int2ObjectMap<CompiledProgram> programs;
    private final Object2ObjectMap<String, ShaderTexture> definitionSamplers;
    private final ObjectSet<ShaderFeature> requiredFeatures;
    private final Object2ObjectMap<CharSequence, ShaderBlock<?>> shaderBlocks;
    private final Object2ObjectMap<String, ShaderUniformImpl> uniforms;
    private final Supplier<Wrapper> wrapper;

    private VertexFormat vertexFormat;
    private ProgramDefinition definition;
    private CompiledProgram compiledProgram;
    private boolean validated;

    public ShaderProgramImpl(ResourceLocation name) {
        this.name = name;
        this.textures = new ShaderTextureCache(this);
        this.programs = new Int2ObjectArrayMap<>(1);
        this.definitionSamplers = new Object2ObjectArrayMap<>();
        this.requiredFeatures = new ObjectArraySet<>();
        this.shaderBlocks = new Object2ObjectArrayMap<>();
        this.uniforms = new Object2ObjectArrayMap<>();
        this.wrapper = Suppliers.memoize(() -> {
            Wrapper.constructingProgram = this;
            try {
                return new Wrapper(this);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to wrap shader program: " + name, e);
            } finally {
                Wrapper.constructingProgram = null;
            }
        });
    }

    /**
     * Saves the current blend state to be restored later with {@link #restoreBlendState()}.
     *
     * @param saveEquation Whether the blend equation needs to be reset after
     */
    public static void saveBlendState(boolean saveEquation) {
        restoreEquation = saveEquation;
        srcColorFactor = GlStateManager.BLEND.srcRgb;
        dstColorFactor = GlStateManager.BLEND.dstRgb;
        srcAlphaFactor = GlStateManager.BLEND.srcAlpha;
        dstAlphaFactor = GlStateManager.BLEND.dstAlpha;
    }

    /**
     * Restores the previously saved blend state.
     */
    public static void restoreBlendState() {
        if (srcColorFactor == -1) {
            return;
        }

        if (restoreEquation) {
            glBlendEquationSeparate(GL_FUNC_ADD, GL_FUNC_ADD);
        }
        GlStateManager._blendFuncSeparate(srcColorFactor, dstColorFactor, srcAlphaFactor, dstAlphaFactor);
        srcColorFactor = -1;
        dstColorFactor = -1;
        srcAlphaFactor = -1;
        dstAlphaFactor = -1;
        restoreEquation = false;
    }

    /**
     * Links and applies the specified shader program.
     *
     * @param program The program to apply
     * @throws ShaderException If there is any problem linking the shader
     */
    protected void applyProgram(CompiledProgram program) throws ShaderException {
        program.link(this);
        this.textures.clear();
        this.compiledProgram = program;
        this.vertexFormat = program.detectVertexFormat();
        this.validated = !Veil.platform().isDevelopmentEnvironment();

        Set<ShaderUniformImpl> old = new HashSet<>(this.uniforms.values());

        // Add all new uniforms
        for (Map.Entry<String, ShaderUniformCache.Uniform> entry : this.compiledProgram.uniformCache.getUniforms().entrySet()) {
            ShaderUniformCache.Uniform data = entry.getValue();
            if (data.type() == GL_UNSIGNED_INT_ATOMIC_COUNTER) {
                continue;
            }

            ShaderUniformImpl uniform = this.getOrCreateUniform(entry.getKey());
            old.remove(uniform);
            uniform.set(data);
        }

        // Invalidate old uniforms
        for (ShaderUniformImpl uniform : old) {
            uniform.set(null);
        }
    }

    protected void attachShaders(CompiledProgram compiledProgram, ShaderSourceSet sourceSet, ShaderCompiler compiler) throws ShaderException, IOException {
        Int2ObjectMap<ResourceLocation> shaders = this.definition.shaders();
        for (Int2ObjectMap.Entry<ResourceLocation> entry : shaders.int2ObjectEntrySet()) {
            int glType = entry.getIntKey();
            ResourceLocation source = entry.getValue();
            compiledProgram.attachShader(glType, compiler.compile(glType, sourceSet.getTypeConverter(glType).idToFile(source)));
        }

        // This allows fragment shaders to be optional
        if (!shaders.containsKey(GL_COMPUTE_SHADER) && !shaders.containsKey(GL_FRAGMENT_SHADER)) {
            compiledProgram.attachShader(GL_FRAGMENT_SHADER, compiler.compile(GL_FRAGMENT_SHADER, DUMMY_FRAGMENT_SHADER));
        }
    }

    public void compile(int activeBuffers, ShaderSourceSet sourceSet, @Nullable ProgramDefinition definition, ShaderCompiler compiler) throws ShaderException, IOException {
        ProgramDefinition old = this.definition;
        this.definition = definition;
        try {
            this.recompile(activeBuffers, sourceSet, compiler);
        } catch (Throwable t) {
            if (old != null) {
                this.definition = old;
            }
            throw t;
        }
        // Compilation was successful, so update the state of this program
        this.definitionSamplers.values().forEach(NativeResource::free);
        this.definitionSamplers.clear();
        this.requiredFeatures.clear();
        if (this.definition != null) {
            for (Map.Entry<String, ShaderTextureSource> entry : this.definition.samplers().entrySet()) {
                this.definitionSamplers.put(entry.getKey(), ShaderTexture.create(entry.getValue()));
            }
            this.requiredFeatures.addAll(Arrays.asList(this.definition.requiredFeatures()));
        }
    }

    public void recompile(int activeBuffers, ShaderSourceSet sourceSet, ShaderCompiler compiler) throws ShaderException, IOException {
        CompiledProgram compiledProgram = CompiledProgram.create(this.name);
        try {
            this.attachShaders(compiledProgram, sourceSet, compiler);

            CompiledProgram old = this.programs.put(activeBuffers, compiledProgram);
            if (old != null) {
                old.free();
            }

            this.applyProgram(compiledProgram);
        } catch (Exception e) {
            if (this.compiledProgram == null) {
                // The initial program failed, so fully fail
                this.freeInternal();
            } else {
                compiledProgram.free();
            }
            throw e;
        }
    }

    /**
     * Sets the active buffers for this shader
     *
     * @param activeBuffers The new active buffers
     * @return Whether this shader needs to be scheduled for a recompilation
     * @throws ShaderException If there is any problem linking the shader
     */
    public boolean setActiveBuffers(int activeBuffers) throws ShaderException {
        CompiledProgram compiledProgram = this.programs.get(activeBuffers);
        if (compiledProgram != null) {
            if (this.compiledProgram != compiledProgram) {
                this.applyProgram(compiledProgram);
            }
            return false;
        }
        return true;
    }

    @Override
    public void bind() {
        VeilRenderSystem.clearShaderBlocks();
        for (Object2ObjectMap.Entry<CharSequence, ShaderBlock<?>> entry : this.shaderBlocks.object2ObjectEntrySet()) {
            VeilRenderSystem.bind(entry.getKey(), entry.getValue());
        }
        ShaderBlendMode blendMode = this.definition != null ? this.definition.blendMode() : null;
        if (blendMode != null) {
            saveBlendState(blendMode.hasEquation());
            blendMode.apply();
        }
        ShaderProgram.super.bind();
        if (this.compiledProgram != null && !this.validated) {
            this.compiledProgram.validate(this);
            this.validated = true;
        }
    }

    @Override
    public void setDefaultUniforms(VertexFormat.Mode mode, Matrix4fc modelViewMatrix, Matrix4fc projectionMatrix) {
        this.wrapper.get().setDefaultUniforms(mode, MODEL_VIEW_MATRIX.set(modelViewMatrix), PROJECTION_MATRIX.set(projectionMatrix), Minecraft.getInstance().getWindow());
    }

    public void freeInternal() {
        this.textures.clear();
        for (CompiledProgram program : this.programs.values()) {
            program.free();
        }
        this.uniforms.values().forEach(ShaderUniformImpl::free);
        this.uniforms.clear();
        this.definitionSamplers.values().forEach(NativeResource::free);
        this.definitionSamplers.clear();
        this.vertexFormat = null;
        this.compiledProgram = null;
    }

    @Override
    public void free() {
        this.freeInternal();
    }

    @Override
    public Int2ObjectMap<CompiledShader> getShaders() {
        return this.compiledProgram != null ? this.compiledProgram.shaders : Int2ObjectMaps.emptyMap();
    }

    @Override
    public boolean isValid() {
        return this.compiledProgram != null;
    }

    @Override
    public Set<ShaderFeature> getRequiredFeatures() {
        return this.requiredFeatures;
    }

    @Override
    public @Nullable VertexFormat getFormat() {
        return this.vertexFormat;
    }

    @Override
    public Set<String> getDefinitionDependencies() {
        return this.compiledProgram != null ? this.compiledProgram.definitionDependencies : Collections.emptySet();
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public Wrapper toShaderInstance() {
        return this.wrapper.get();
    }

    @Override
    public int getUniformLocation(CharSequence name) {
        if (this.compiledProgram == null) {
            return -1;
        }
        ShaderUniformCache.Uniform uniform = this.compiledProgram.uniformCache.getUniform(name);
        return uniform != null ? uniform.location() : -1;
    }

    @Override
    public @Nullable ShaderUniformImpl getUniform(CharSequence name) {
        ShaderUniformImpl uniform = this.uniforms.get(name.toString());
        return uniform != null && uniform.isValid() ? uniform : null;
    }

    @Override
    public ShaderUniformAccess getUniformSafe(CharSequence name) {
        ShaderUniformImpl uniform = this.uniforms.get(name.toString());
        return uniform != null && uniform.isValid() ? uniform : ShaderUniformAccess.EMPTY;
    }

    @Override
    public ShaderUniformImpl getOrCreateUniform(CharSequence name) {
        return this.uniforms.computeIfAbsent(name.toString(), key -> new ShaderUniformImpl(this::getProgram, (String) key));
    }

    @Override
    public boolean hasUniform(CharSequence name) {
        return this.compiledProgram != null && this.compiledProgram.uniformCache.hasUniform(name.toString());
    }

    @Override
    public int getUniformBlock(CharSequence name) {
        if (this.compiledProgram == null) {
            return GL_INVALID_INDEX;
        }
        ShaderUniformCache.UniformBlock block = this.compiledProgram.uniformCache.getUniformBlock(name.toString());
        return block != null ? block.index() : GL_INVALID_INDEX;
    }

    @Override
    public boolean hasUniformBlock(CharSequence name) {
        return this.compiledProgram != null && this.compiledProgram.uniformCache.hasUniformBlock(name.toString());
    }

    @Override
    public int getStorageBlock(CharSequence name) {
        if (this.compiledProgram == null) {
            return GL_INVALID_INDEX;
        }
        ShaderUniformCache.StorageBlock block = this.compiledProgram.uniformCache.getStorageBlock(name.toString());
        return block != null ? block.index() : GL_INVALID_INDEX;
    }

    @Override
    public boolean hasStorageBlock(CharSequence name) {
        return this.compiledProgram != null && this.compiledProgram.uniformCache.hasStorageBlock(name.toString());
    }

    @Override
    public int getProgram() {
        return this.compiledProgram != null ? this.compiledProgram.program : 0;
    }

    @Override
    public @Nullable ProgramDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public void bindSamplers(@Nullable ShaderTextureSource.Context context, int samplerStart) {
        if (this.compiledProgram == null) {
            return;
        }

        if (context != null) {
            for (Map.Entry<String, ShaderTexture> entry : this.definitionSamplers.entrySet()) {
                ShaderTexture source = entry.getValue();
                this.setSampler(entry.getKey(), source.textureSource.getId(context), source.samplerId());
            }
        }
        this.textures.bind(this.compiledProgram.uniformCache, samplerStart);
    }

    @Override
    public void setSampler(CharSequence name, int textureId, int samplerId) {
        if (this.compiledProgram != null && this.compiledProgram.uniformCache.hasSampler(name.toString())) {
            this.textures.put(name, textureId, samplerId);
        }
    }

    @Override
    public void removeSampler(CharSequence name) {
        this.textures.remove(name);
    }

    @Override
    public void clearSamplers() {
        this.textures.clear();
    }

    public void addShaderBlock(String name, ShaderBlock<?> block) {
        this.shaderBlocks.put(name, block);
    }

    public void clearShaderBlocks() {
        this.shaderBlocks.clear();
    }

    public record CompiledProgram(int program,
                                  Int2ObjectMap<CompiledShader> shaders,
                                  Int2ObjectMap<CompiledShader> shadersView,
                                  ShaderUniformCache uniformCache,
                                  Set<String> definitionDependencies) implements NativeResource {

        public static CompiledProgram create(ResourceLocation id) {
            int program = glCreateProgram();
            VeilDebug.get().objectLabel(GL_PROGRAM, program, "Shader Program " + id);
            Int2ObjectMap<CompiledShader> shaders = new Int2ObjectArrayMap<>(2);
            Int2ObjectMap<CompiledShader> shadersView = Int2ObjectMaps.unmodifiable(shaders);
            ShaderUniformCache uniforms = new ShaderUniformCache(() -> program);
            Set<String> definitionDependencies = new HashSet<>();
            return new CompiledProgram(program, shaders, shadersView, uniforms, definitionDependencies);
        }

        public void attachShader(int glType, CompiledShader shader) {
            CompiledShader old = this.shaders.put(glType, shader);
            if (old != null) {
                old.free();
            }
            glAttachShader(this.program, shader.id());
        }

        public @Nullable VertexFormat detectVertexFormat() {
            VertexFormat best = null;
            int bestElements = 0;

            int activeAttributes = glGetProgrami(this.program, GL_ACTIVE_ATTRIBUTES);
            Int2ObjectMap<String> names = new Int2ObjectArrayMap<>(activeAttributes);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer size = stack.mallocInt(1);
                IntBuffer type = stack.mallocInt(1);
                for (int i = 0; i < activeAttributes; i++) {
                    String name = glGetActiveAttrib(this.program, i, size, type);
                    names.put(glGetAttribLocation(this.program, name), name);
                }
            }

            for (VertexFormat format : VertexFormatCodec.getDefaultFormats().values()) {
                List<VertexFormatElement> elements = format.getElements();
                int foundElements = 0;
                if (elements.size() > activeAttributes) {
                    continue;
                }

                for (int i = 0; i < elements.size(); i++) {
                    if (!format.getElementName(elements.get(i)).equals(names.get(i))) {
                        break;
                    }
                    foundElements++;
                }

                if (foundElements < elements.size()) {
                    continue;
                }

                if (bestElements <= activeAttributes && foundElements > bestElements) {
                    best = format;
                    bestElements = foundElements;
                }
            }

            return best;
        }

        public void validate(ShaderProgram shaderProgram) {
            glValidateProgram(this.program);
            if (glGetProgrami(this.program, GL_VALIDATE_STATUS) != GL_TRUE) {
                String log = StringUtils.trim(glGetProgramInfoLog(this.program));
                Veil.LOGGER.warn("Failed to validate shader ({}) : {}", shaderProgram.getName(), log);
            }
        }

        public void link(ShaderProgram shaderProgram) throws ShaderException {
            glLinkProgram(this.program);
            if (glGetProgrami(this.program, GL_LINK_STATUS) != GL_TRUE) {
                String log = StringUtils.trim(glGetProgramInfoLog(this.program));
                throw new ShaderException("Failed to link shader", log);
            }

            this.uniformCache.clear();
            this.definitionDependencies.clear();
            this.shaders.values().forEach(shader -> {
                shader.apply(shaderProgram);
                this.definitionDependencies.addAll(shader.definitionDependencies());
            });
        }

        @Override
        public void free() {
            for (CompiledShader shader : this.shaders.values()) {
                shader.free();
            }
            this.shaders.clear();
            glDeleteProgram(this.program);
            this.uniformCache.clear();
            this.definitionDependencies.clear();
        }
    }

    public record ShaderTexture(ShaderTextureSource textureSource,
                                @Nullable SamplerObject sampler) implements NativeResource {

        public static ShaderTexture create(ShaderTextureSource source) {
            TextureFilter filter = source.filter();
            SamplerObject samplerObject;
            if (filter != null) {
                samplerObject = SamplerObject.create();
                samplerObject.setFilter(filter);
            } else {
                samplerObject = null;
            }
            return new ShaderTexture(source, samplerObject);
        }

        @Override
        public void free() {
            if (this.sampler != null) {
                this.sampler.free();
            }
        }

        public int samplerId() {
            return this.sampler != null ? this.sampler.getId() : 0;
        }
    }

    private static class DummyShaderResource extends Resource {

        private static final byte[] DUMMY_SHADER = """
                {
                    "vertex": "dummy",
                    "fragment": "dummy"
                }
                """.getBytes(StandardCharsets.UTF_8);

        public DummyShaderResource() {
            super(null, () -> new ByteArrayInputStream(DUMMY_SHADER));
        }

        @Override
        public PackResources source() {
            throw new UnsupportedOperationException("No pack source");
        }

        @Override
        public String sourcePackId() {
            return "dummy";
        }

        @Override
        public Optional<KnownPack> knownPackInfo() {
            return Optional.empty();
        }
    }

    /**
     * @author Ocelot
     */
    public static class Wrapper extends ShaderInstance {

        private static final Resource RESOURCE = new DummyShaderResource();
        private static final VertexFormat DUMMY_FORMAT = VertexFormat.builder().build();

        public static ShaderProgram constructingProgram = null;

        private final ShaderProgram program;

        private Wrapper(ShaderProgram program) throws IOException {
            super(name -> Optional.of(RESOURCE), "", DUMMY_FORMAT);
            this.program = program;
        }

        @Override
        public void close() {
        }

        @Override
        public void clear() {
            ShaderProgram.unbind();
        }

        @Override
        public void apply() {
            this.program.bind();
            this.program.bindSamplers(0);
        }

        @Override
        public void attachToProgram() {
            throw new UnsupportedOperationException("Cannot attach shader program wrapper");
        }

        @Override
        public void markDirty() {
        }

        @Override
        public @Nullable UniformWrapper getUniform(String name) {
            if (this.program != null && this.program.getUniformLocation(name) == -1) {
                return null;
            }
            return (UniformWrapper) this.uniformMap.computeIfAbsent(name, unused -> new UniformWrapper(name, () -> Objects.requireNonNull(this.program).getOrCreateUniform(name)));
        }

        @Override
        public void setSampler(@NotNull String name, Object value) {
            int sampler = switch (value) {
                case RenderTarget target -> target.getColorTextureId();
                case AbstractTexture texture -> texture.getId();
                case Integer id -> id;
                default -> -1;
            };

            if (sampler != -1) {
                if (sampler == 0) {
                    this.program.removeSampler(name);
                } else {
                    this.program.setSampler(name, sampler);
                }
            }
        }

        @Override
        public VertexFormat getVertexFormat() {
            VertexFormat format = this.program.getFormat();
            return format != null ? format : super.getVertexFormat();
        }

        /**
         * @return The backing shader program
         */
        public ShaderProgram program() {
            return this.program;
        }
    }

    /**
     * @author Ocelot
     */
    public static class UniformWrapper extends Uniform {

        private static final Matrix2f MAT2X2 = new Matrix2f();
        private static final Matrix3f MAT3X3 = new Matrix3f();
        private static final Matrix3x2f MAT3X2 = new Matrix3x2f();
        private static final Matrix4x3f MAT4X3 = new Matrix4x3f();
        private static final Matrix4f MAT4X4 = new Matrix4f();

        private final Supplier<ShaderUniform> uniform;

        public UniformWrapper(String name, Supplier<ShaderUniform> uniform) {
            super(name, UT_INT1, 0, null);
            super.close(); // Free constructor allocated resources
            this.uniform = Suppliers.memoize(uniform::get);
        }

        @Override
        public void setLocation(int location) {
        }

        @Override
        public void set(int index, float value) {
            throw new UnsupportedOperationException("Use absolute set");
        }

        @Override
        public void set(float value) {
            this.uniform.get().setFloat(value);
        }

        @Override
        public void set(float x, float y) {
            this.uniform.get().setVector(x, y);
        }

        @Override
        public void set(float x, float y, float z) {
            this.uniform.get().setVector(x, y, z);
        }

        @Override
        public void set(float x, float y, float z, float w) {
            this.uniform.get().setVector(x, y, z, w);
        }

        @Override
        public void set(@NotNull Vector3f value) {
            this.uniform.get().setVector(value);
        }

        @Override
        public void set(@NotNull Vector4f value) {
            this.uniform.get().setVector(value);
        }

        @Override
        public void setSafe(float x, float y, float z, float w) {
            this.set(x, y, z, w);
        }

        @Override
        public void set(int value) {
            this.uniform.get().setInt(value);
        }

        @Override
        public void set(int x, int y) {
            this.uniform.get().setVectorI(x, y);
        }

        @Override
        public void set(int x, int y, int z) {
            this.uniform.get().setVectorI(x, y, z);
        }

        @Override
        public void set(int x, int y, int z, int w) {
            this.uniform.get().setVectorI(x, y, z, w);
        }

        @Override
        public void setSafe(int x, int y, int z, int w) {
            this.set(x, y, z, w);
        }

        @Override
        public void set(float[] values) {
            this.uniform.get().setVector(values);
        }

        @Override
        public void setMat2x2(float m00, float m01, float m10, float m11) {
            this.uniform.get().setMatrix(MAT2X2.set(m00, m01, m10, m11), false);
        }

        @Override
        public void setMat2x3(float m00, float m01, float m02, float m10, float m11, float m12) {
            this.uniform.get().setMatrix2x3(MAT3X2.set(
                    m00, m10,
                    m01, m11,
                    m02, m12
            ), false);
        }

        @Override
        public void setMat2x4(float m00, float m01, float m02, float m03, float m10, float m11, float m12, float m13) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMat3x2(float m00, float m01, float m10, float m11, float m20, float m21) {
            this.uniform.get().setMatrix3x2(MAT3X2.set(m00, m01, m10, m11, m20, m21), false);
        }

        @Override
        public void setMat3x3(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
            this.uniform.get().setMatrix(MAT3X3.set(m00, m01, m02, m10, m11, m12, m20, m21, m22), false);
        }

        @Override
        public void setMat3x4(
                float m00,
                float m01,
                float m02,
                float m03,
                float m10,
                float m11,
                float m12,
                float m13,
                float m20,
                float m21,
                float m22,
                float m23
        ) {
            this.uniform.get().setMatrix3x4(MAT4X3.set(
                    m00, m10, m20,
                    m01, m11, m21,
                    m02, m12, m22,
                    m03, m13, m23
            ), false);
        }

        @Override
        public void setMat4x3(
                float m00,
                float m01,
                float m02,
                float m03,
                float m10,
                float m11,
                float m12,
                float m13,
                float m20,
                float m21,
                float m22,
                float m23) {
            this.uniform.get().setMatrix4x3(MAT4X3.set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23), false);
        }

        @Override
        public void setMat4x2(float m00, float m01, float m02, float m03, float m10, float m11, float m12, float m13) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMat4x4(
                float m00,
                float m01,
                float m02,
                float m03,
                float m10,
                float m11,
                float m12,
                float m13,
                float m20,
                float m21,
                float m22,
                float m23,
                float m30,
                float m31,
                float m32,
                float m33
        ) {
            this.uniform.get().setMatrix(MAT4X4.set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33), false);
        }

        @Override
        public void set(@NotNull Matrix3f value) {
            this.uniform.get().setMatrix(value, false);
        }

        @Override
        public void set(@NotNull Matrix4f value) {
            this.uniform.get().setMatrix(value, false);
        }

        @Override
        public void upload() {
        }

        @Override
        public void close() {
        }

        @Override
        public int getLocation() {
            return this.uniform.get().getLocation();
        }
    }

    public static class ShaderWrapper extends Program {

        private final Type type;
        private final ShaderProgram program;

        public ShaderWrapper(Type type, ShaderProgram program) {
            super(type, 0, getName(type, program));
            this.type = type;
            this.program = program;
        }

        private static String getName(Type type, ShaderProgram program) {
            ProgramDefinition definition = program.getDefinition();
            if (definition != null) {
                switch (type) {
                    case VERTEX -> {
                        ResourceLocation vertex = definition.vertex();
                        if (vertex != null) {
                            return vertex.toString();
                        }
                    }
                    case FRAGMENT -> {
                        ResourceLocation fragment = definition.fragment();
                        if (fragment != null) {
                            return fragment.toString();
                        }
                    }
                }
            }
            return Veil.MODID + ":dummy_" + type.getName();
        }

        @Override
        public void attachToShader(Shader shader) {
        }

        @Override
        public void close() {
        }

        @Override
        public String getName() {
            return getName(this.type, this.program);
        }

        @Override
        public int getId() {
            Int2ObjectMap<CompiledShader> shaders = this.program.getShaders();
            switch (this.type) {
                case VERTEX -> {
                    CompiledShader vertex = shaders.get(GL_VERTEX_SHADER);
                    return vertex != null ? vertex.id() : 0;
                }
                case FRAGMENT -> {
                    CompiledShader fragment = shaders.get(GL_FRAGMENT_SHADER);
                    return fragment != null ? fragment.id() : 0;
                }
            }
            return super.getId();
        }
    }
}
