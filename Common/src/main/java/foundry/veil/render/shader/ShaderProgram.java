package foundry.veil.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.render.shader.compiler.CompiledShader;
import foundry.veil.render.shader.compiler.ShaderCompiler;
import foundry.veil.render.shader.texture.ShaderTextureSource;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;

import java.util.*;

import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL31C.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;

/**
 * Represents a usable shader program with shaders attached.
 *
 * @author Ocelot
 */
public class ShaderProgram implements NativeResource, MutableUniformAccess {

    private final ResourceLocation id;
    private final Set<Integer> shaders;
    private final Map<CharSequence, Integer> uniforms;
    private final Map<CharSequence, Integer> blocks;
    private final Map<CharSequence, Integer> textures;
    private final Map<String, ShaderTextureSource> textureSources;
    private final Set<String> definitionDependencies;
    private int program;

    /**
     * Creates a new shader program with the specified id.
     *
     * @param id The id of the program
     */
    public ShaderProgram(@NotNull ResourceLocation id) {
        this.id = Objects.requireNonNull(id, "name");
        this.shaders = new HashSet<>(2);
        this.uniforms = new Object2IntArrayMap<>();
        this.blocks = new Object2IntArrayMap<>();
        this.textures = new HashMap<>();
        this.textureSources = new HashMap<>();
        this.definitionDependencies = new HashSet<>();
    }

    private void clearShader() {
        this.shaders.clear();
        this.uniforms.clear();
        this.blocks.clear();
        this.textures.clear();
        this.textureSources.clear();
        this.definitionDependencies.clear();
    }

    /**
     * Binds this program for use.
     */
    public void bind() {
        glUseProgram(this.program);
    }

    /**
     * Unbinds the currently bound shader program.
     */
    public static void unbind() {
        glUseProgram(0);
    }

    /**
     * Compiles this shader based on the specified definition.
     *
     * @param context  The context to use when compiling shaders
     * @param compiler The compiler to use
     * @throws Exception If an error occurs while compiling or linking shaders
     */
    public void compile(@NotNull ShaderCompiler.Context context, @NotNull ShaderCompiler compiler) throws Exception {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(compiler, "compiler");
        ProgramDefinition definition = Objects.requireNonNull(context.definition(), "definition");

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
            if (Minecraft.ON_OSX && !shaders.containsKey(GL_COMPUTE_SHADER) && !shaders.containsKey(GL_FRAGMENT_SHADER)) {
                CompiledShader shader = compiler.compile(context, GL_FRAGMENT_SHADER, "out vec4 fragColor;void main(){fragColor=vec4(1.0);}");
                glAttachShader(this.program, shader.id());
                this.shaders.add(GL_FRAGMENT_SHADER);
                compiledShaders.add(shader);
            }

            glLinkProgram(this.program);
            if (glGetProgrami(this.program, GL_LINK_STATUS) != GL_TRUE) {
                throw new ShaderException("Failed to link shader", glGetProgramInfoLog(this.program));
            }

            this.bind();
            compiledShaders.forEach(shader -> {
                glDetachShader(this.program, shader.id()); // Detach to allow the shaders to be deleted
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

    /**
     * @return An immutable view of the shader types attached to this program
     */
    public @NotNull Set<Integer> getShaders() {
        return this.shaders;
    }

    /**
     * @return All shader definitions this program depends on
     */
    public @NotNull Set<String> getDefinitionDependencies() {
        return this.definitionDependencies;
    }

    /**
     * @return The id of this program
     */
    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    @Override
    public int getUniform(@NotNull CharSequence name) {
        Objects.requireNonNull(name, "name");
        return this.uniforms.computeIfAbsent(name, k -> glGetUniformLocation(this.program, k));
    }

    @Override
    public int getUniformBlock(@NotNull CharSequence name) {
        Objects.requireNonNull(name, "name");
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

        if (this.textures.isEmpty()) {
            return sampler;
        }

        int activeTexture = GlStateManager._getActiveTexture();
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
    public void addSampler(@NotNull CharSequence name, int textureId) {
        Objects.requireNonNull(name, "name");
        this.textures.put(name, textureId);
    }

    @Override
    public void removeSampler(@NotNull CharSequence name) {
        Objects.requireNonNull(name, "name");
        this.textures.remove(name);
    }

    @Override
    public void clearSamplers() {
        this.textures.clear();
    }
}
