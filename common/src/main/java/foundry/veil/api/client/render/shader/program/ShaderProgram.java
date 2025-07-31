package foundry.veil.api.client.render.shader.program;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.ShaderFeature;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.compiler.CompiledShader;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import foundry.veil.impl.client.render.shader.program.ShaderProgramImpl;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.lwjgl.system.NativeResource;

import java.util.Set;

import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL31C.GL_INVALID_INDEX;
import static org.lwjgl.opengl.GL31C.glUniformBlockBinding;
import static org.lwjgl.opengl.GL41C.*;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;
import static org.lwjgl.opengl.GL43C.glShaderStorageBlockBinding;

/**
 * Represents a usable shader program with shaders attached.
 *
 * @author Ocelot
 */
@ApiStatus.NonExtendable
public interface ShaderProgram extends NativeResource, MutableUniformAccess, TextureUniformAccess {

    /**
     * Binds this program for use.
     */
    default void bind() {
        int program = this.getProgram();
        if (ShaderInstance.lastProgramId != program) {
            ShaderInstance.lastProgramId = program;
            GlStateManager._glUseProgram(program);
        }
        EffectInstance.lastProgramId = -1;
    }

    /**
     * Unbinds the currently bound shader program.
     */
    static void unbind() {
        VeilRenderSystem.clearShaderBlocks();
        GlStateManager._glUseProgram(0);
        ShaderInstance.lastProgramId = -1;
        EffectInstance.lastProgramId = -1;
        VeilRenderSystem.unbindSamplers(0, VeilRenderSystem.maxCombinedTextureUnits());
        ShaderProgramImpl.restoreBlendState();
    }

    /**
     * Sets the default uniforms in this shader.
     *
     * @param mode The expected draw mode
     */
    default void setDefaultUniforms(VertexFormat.Mode mode) {
        this.setDefaultUniforms(mode, RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix());
    }

    /**
     * Sets the default uniforms in this shader.
     *
     * @param mode             The expected draw mode
     * @param modelViewMatrix  The view matrix transform
     * @param projectionMatrix The projection matrix transform
     */
    void setDefaultUniforms(VertexFormat.Mode mode, Matrix4fc modelViewMatrix, Matrix4fc projectionMatrix);

    /**
     * @return The OpenGL id of this program
     */
    int getProgram();

    @Override
    ShaderUniform getUniform(CharSequence name);

    @Override
    ShaderUniform getOrCreateUniform(CharSequence name);

    @Override
    default void setUniformBlock(CharSequence name, int binding) {
        int index = this.getUniformBlock(name);
        if (index != GL_INVALID_INDEX) {
            glUniformBlockBinding(this.getProgram(), index, binding);
        }
    }

    @Override
    default void setStorageBlock(CharSequence name, int binding) {
        int index = this.getStorageBlock(name);
        if (index != GL_INVALID_INDEX) {
            glShaderStorageBlockBinding(this.getProgram(), index, binding);
        }
    }

    /**
     * @return The definition used to compile the latest version of this shader
     */
    @Nullable
    ProgramDefinition getDefinition();

    /**
     * @return The shaders attached to this program
     */
    Int2ObjectMap<CompiledShader> getShaders();

    /**
     * @return Whether this program has a valid compiled shader
     * @since 2.0.0
     */
    boolean isValid();

    /**
     * @return Whether this program has the vertex stage
     */
    default boolean hasVertex() {
        return this.getShaders().containsKey(GL_VERTEX_SHADER);
    }

    /**
     * @return Whether this program has the geometry stage
     */
    default boolean hasGeometry() {
        return this.getShaders().containsKey(GL_GEOMETRY_SHADER);
    }

    /**
     * @return Whether this program has the fragment stage
     */
    default boolean hasFragment() {
        return this.getShaders().containsKey(GL_VERTEX_SHADER);
    }

    /**
     * @return Whether this program has the tesselation stages
     */
    default boolean hasTesselation() {
        Int2ObjectMap<CompiledShader> shaders = this.getShaders();
        return shaders.containsKey(GL_TESS_CONTROL_SHADER) && shaders.containsKey(GL_TESS_EVALUATION_SHADER);
    }

    /**
     * @return Whether this program has the compute stage
     */
    default boolean isCompute() {
        return this.getShaders().containsKey(GL_COMPUTE_SHADER);
    }

    /**
     * @return The features this program needs to function
     * @since 2.0.0
     */
    Set<ShaderFeature> getRequiredFeatures();

    /**
     * @return A guess at the best vertex format for this program
     */
    @Nullable
    VertexFormat getFormat();

    /**
     * @return All shader definitions this program depends on
     */
    Set<String> getDefinitionDependencies();

    /**
     * @return The name of this program
     */
    ResourceLocation getName();

    /**
     * <p>Wraps this shader with a vanilla Minecraft shader instance wrapper. There are a few special properties about the shader wrapper.</p>
     * <ul>
     *     <li>The shader instance cannot be used to free the shader program. {@link ShaderProgram#free()} must be called separately.
     *     If the shader is loaded through {@link ShaderManager} then there is no need to free the shader.</li>
     *     <li>Calling {@link Uniform#upload()} will do nothing since the values are uploaded when the appropriate methods are called</li>
     *     <li>Uniforms are lazily wrapped and will not crash when the wrong method is called.</li>
     *     <li>{@link Uniform#set(int, float)} is not supported and will throw an {@link UnsupportedOperationException}.</li>
     *     <li>{@link Uniform#set(float[])} only works for 1, 2, 3, and 4 float elements. Any other size will throw an {@link UnsupportedOperationException}.</li>
     * </ul>
     *
     * @return A lazily loaded shader instance wrapper for this program
     * @deprecated Use {@link VeilRenderBridge#toShaderInstance(ShaderProgram)}
     */
    @Deprecated
    ShaderInstance toShaderInstance();
}
