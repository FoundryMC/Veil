package foundry.veil.render.shader.program;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.render.shader.ShaderException;
import foundry.veil.render.shader.compiler.CompiledShader;
import foundry.veil.render.shader.compiler.ShaderCompiler;
import foundry.veil.render.shader.texture.ShaderTextureSource;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL31C.GL_INVALID_INDEX;
import static org.lwjgl.opengl.GL31C.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class ShaderProgramImpl implements ShaderProgram {

    private final ResourceLocation id;
    private final Set<Integer> shaders;
    private final Map<CharSequence, Integer> uniforms;
    private final Map<CharSequence, Integer> blocks;
    private final Map<CharSequence, Integer> textures;
    private final Map<String, ShaderTextureSource> textureSources;
    private final Set<String> definitionDependencies;
    private final Supplier<Wrapper> wrapper;
    private int program;

    ShaderProgramImpl(ResourceLocation id) {
        this.id = id;
        this.shaders = new HashSet<>(2);
        this.uniforms = new Object2IntArrayMap<>();
        this.blocks = new Object2IntArrayMap<>();
        this.textures = new HashMap<>();
        this.textureSources = new HashMap<>();
        this.definitionDependencies = new HashSet<>();
        this.wrapper = Suppliers.memoize(() -> {
            Wrapper.constructing = true;
            try {
                return new Wrapper(this);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to wrap shader program: " + this.getId(), e);
            } finally {
                Wrapper.constructing = false;
            }
        });
    }

    private void clearShader() {
        this.shaders.clear();
        this.uniforms.clear();
        this.blocks.clear();
        this.textures.clear();
        this.textureSources.clear();
        this.definitionDependencies.clear();
    }

    @Override
    public void compile(ShaderCompiler.Context context, ShaderCompiler compiler) throws Exception {
        ProgramDefinition definition = Objects.requireNonNull(context.definition());

        this.clearShader();
        this.textureSources.putAll(definition.textures());

        if (this.program == 0) {
            this.program = glCreateProgram();
        }

        try {
            Set<CompiledShader> compiledShaders = new HashSet<>();

            Map<Integer, ResourceLocation> shaders = definition.shaders();
            for (Map.Entry<Integer, ResourceLocation> entry : shaders.entrySet()) {
                CompiledShader shader = compiler.compile(context, entry.getKey(), entry.getValue());
                glAttachShader(this.program, shader.id());
                this.shaders.add(entry.getKey());
                compiledShaders.add(shader);
            }

            // Fragment shaders aren't strictly necessary if the fragment output isn't used,
            // however mac shaders just don't work without a fragment, shader. This adds a "dummy" fragment shader
            // on mac specifically for all rendering shaders.
            if (Minecraft.ON_OSX && !shaders.containsKey(GL_COMPUTE_SHADER) &&
                !shaders.containsKey(GL_FRAGMENT_SHADER)) {
                CompiledShader shader = compiler.compile(context, GL_FRAGMENT_SHADER,
                    "out vec4 fragColor;void main(){fragColor=vec4(1.0);}");
                glAttachShader(this.program, shader.id());
                this.shaders.add(GL_FRAGMENT_SHADER);
                compiledShaders.add(shader);
            }

            glLinkProgram(this.program);
            if (glGetProgrami(this.program, GL_LINK_STATUS) != GL_TRUE) {
                String log = glGetProgramInfoLog(this.program);
                throw new ShaderException("Failed to link shader", log);
            }

            this.bind();
            compiledShaders.forEach(shader -> {
                shader.apply(this);
                this.definitionDependencies.addAll(shader.definitionDependencies());
            });
            ShaderProgram.unbind();
        } catch (Exception e) {
            this.free(); // F
            throw e;
        }
    }

    @Override
    public void free() {
        if (this.program > 0) {
            glDeleteProgram(this.program);
            this.program = 0;
        }
        this.clearShader();
    }

    @Override
    public Set<Integer> getShaders() {
        return this.shaders;
    }

    @Override
    public Set<String> getDefinitionDependencies() {
        return this.definitionDependencies;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public Wrapper toShaderInstance() {
        return this.wrapper.get();
    }

    @Override
    public int getUniform(CharSequence name) {
        if (this.program == 0) {
            return -1;
        }
        return this.uniforms.computeIfAbsent(name, k -> glGetUniformLocation(this.program, k));
    }

    @Override
    public int getUniformBlock(CharSequence name) {
        if (this.program == 0) {
            return GL_INVALID_INDEX;
        }
        return this.blocks.computeIfAbsent(name, k -> glGetUniformBlockIndex(this.program, name));
    }

    @Override
    public int getProgram() {
        return this.program;
    }

    @Override
    public int applyShaderSamplers(@Nullable ShaderTextureSource.Context context, int sampler) {
        if (context != null) {
            this.textureSources.forEach((name, source) -> this.addSampler(name, source.getId(context)));
        }

        int activeTexture = GlStateManager._getActiveTexture();

        // Bind missing texture to 0
        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(MissingTextureAtlasSprite.getTexture().getId());
        sampler++;

        for (Map.Entry<CharSequence, Integer> entry : this.textures.entrySet()) {
            if (this.getUniform(entry.getKey()) == -1) {
                continue;
            }

            RenderSystem.activeTexture(GL_TEXTURE0 + sampler);
            RenderSystem.bindTexture(entry.getValue());
            this.setInt(entry.getKey(), sampler);
            sampler++;
        }
        RenderSystem.activeTexture(activeTexture);
        return sampler;
    }

    @Override
    public void addSampler(CharSequence name, int textureId) {
        this.textures.put(name, textureId);
    }

    @Override
    public void removeSampler(CharSequence name) {
        this.textures.remove(name);
    }

    @Override
    public void clearSamplers() {
        this.textures.clear();
    }

    /**
     * @author Ocelot
     */
    public static class Wrapper extends ShaderInstance {

        private static final byte[] DUMMY_SHADER = """
            {
                "vertex": "dummy",
                "fragment": "dummy"
            }
            """.getBytes(StandardCharsets.UTF_8);
        private static final Resource RESOURCE = new Resource(null, () -> new ByteArrayInputStream(DUMMY_SHADER)) {
            @Override
            public PackResources source() {
                throw new UnsupportedOperationException("No pack source");
            }

            @Override
            public String sourcePackId() {
                return "dummy";
            }

            @Override
            public boolean isBuiltin() {
                return true;
            }
        };

        public static boolean constructing = false;

        private final ShaderProgram program;

        private Wrapper(ShaderProgram program) throws IOException {
            super(name -> Optional.of(RESOURCE), "", null);
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
            this.program.addRenderSystemTextures();
            this.program.applyShaderSamplers(0);
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
            UniformWrapper uniform = (UniformWrapper) this.uniformMap.get(name);
            if (uniform != null) {
                return uniform.getLocation() == -1 ? null : uniform;
            }

            // program is null in the constructor, so this allows the default uniforms to be accessed
            if (this.program != null && this.program.getUniform(name) == -1) {
                return null;
            }
            return (UniformWrapper) this.uniformMap.computeIfAbsent(name,
                unused -> new UniformWrapper(() -> this.program, name));
        }

        @Override
        public void setSampler(String name, Object value) {
            int sampler = -1;
            if (value instanceof RenderTarget target) {
                sampler = target.getColorTextureId();
            } else if (value instanceof AbstractTexture texture) {
                sampler = texture.getId();
            } else if (value instanceof Integer id) {
                sampler = id;
            }

            if (sampler != -1) {
                this.program.addSampler(name, sampler);
            }
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

        private final Supplier<MutableShaderUniformAccess> access;

        public UniformWrapper(Supplier<MutableShaderUniformAccess> access, String name) {
            super(name, UT_INT1, 0, null);
            this.close(); // Free constructor allocated resources
            this.access = access;
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
            this.access.get().setFloat(this.getName(), value);
        }

        @Override
        public void set(float x, float y) {
            this.access.get().setVector(this.getName(), x, y);
        }

        @Override
        public void set(float x, float y, float z) {
            this.access.get().setVector(this.getName(), x, y, z);
        }

        @Override
        public void set(float x, float y, float z, float w) {
            this.access.get().setVector(this.getName(), x, y, z, w);
        }

        @Override
        public void set(@NotNull Vector3f value) {
            this.access.get().setVector(this.getName(), value);
        }

        @Override
        public void set(@NotNull Vector4f value) {
            this.access.get().setVector(this.getName(), value);
        }

        @Override
        public void setSafe(float x, float y, float z, float w) {
            this.set(x, y, z, w);
        }

        @Override
        public void set(int value) {
            this.access.get().setInt(this.getName(), value);
        }

        @Override
        public void set(int x, int y) {
            this.access.get().setVector(this.getName(), x, y);
        }

        @Override
        public void set(int x, int y, int z) {
            this.access.get().setVector(this.getName(), x, y, z);
        }

        @Override
        public void set(int x, int y, int z, int w) {
            this.access.get().setVector(this.getName(), x, y, z, w);
        }

        @Override
        public void setSafe(int x, int y, int z, int w) {
            this.set(x, y, z, w);
        }

        @Override
        public void set(float[] values) {
            switch (values.length) {
                case 1 -> this.set(values[0]);
                case 2 -> this.set(values[0], values[1]);
                case 3 -> this.set(values[0], values[1], values[2]);
                case 4 -> this.set(values[0], values[1], values[2], values[3]);
                default -> throw new UnsupportedOperationException("Invalid value array: " + Arrays.toString(values));
            }
        }

        @Override
        public void setMat2x2(float $$0, float $$1, float $$2, float $$3) {
            throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
        }

        @Override
        public void setMat2x3(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5) {
            throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
        }

        @Override
        public void setMat2x4(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7) {
            throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
        }

        @Override
        public void setMat3x2(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5) {
            throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
        }

        @Override
        public void setMat3x3(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7,
                              float $$8) {
            throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
        }

        @Override
        public void setMat3x4(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7,
                              float $$8, float $$9, float $$10, float $$11) {
            throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
        }

        @Override
        public void setMat4x2(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7) {
            throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
        }

        @Override
        public void setMat4x3(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7,
                              float $$8, float $$9, float $$10, float $$11) {
            throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
        }

        @Override
        public void setMat4x4(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7,
                              float $$8, float $$9, float $$10, float $$11, float $$12, float $$13, float $$14,
                              float $$15) {
            throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
        }

        @Override
        public void set(@NotNull Matrix3f value) {
            this.access.get().setMatrix(this.getName(), value);
        }

        @Override
        public void set(@NotNull Matrix4f value) {
            this.access.get().setMatrix(this.getName(), value);
        }

        @Override
        public void upload() {
        }

        @Override
        public int getLocation() {
            return this.access.get().getUniform(this.getName());
        }
    }
}
