package foundry.veil.api.client.render.post;

import com.mojang.serialization.Codec;
import foundry.veil.api.client.registry.PostPipelineStageRegistry;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.shader.program.MutableUniformAccess;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.program.TextureUniformAccess;
import foundry.veil.api.client.render.shader.program.UniformAccess;
import foundry.veil.api.client.render.shader.texture.ShaderTextureSource;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;

/**
 * <p>A series of post-processing effects that can be run to change the current framebuffer state.</p>
 * <p>It can be fully run using {@link PostProcessingManager#runPipeline(PostPipeline)}.</p>
 * <p>This class implements {@link UniformAccess} to allow changing uniforms in all shaders.</p>
 *
 * @author Ocelot
 */
public interface PostPipeline extends MutableUniformAccess, NativeResource {

    Codec<PostPipeline> CODEC = PostPipelineStageRegistry.REGISTRY.byNameCodec().dispatch(PostPipeline::getType, PostPipelineStageRegistry.PipelineType::codec);

    /**
     * Applies this post effect.
     * {@link PostProcessingManager#runPipeline(PostPipeline)} should be called to run this pipeline.
     *
     * @param context The context to use when running this pipeline.
     */
    @ApiStatus.OverrideOnly
    void apply(Context context);

    /**
     * Allows a post pipeline to dispose of any resources it takes up.
     */
    @Override
    default void free() {
    }

    /**
     * @return The type of post effect this is
     */
    PostPipelineStageRegistry.PipelineType<? extends PostPipeline> getType();

    @Override
    default int getUniformLocation(CharSequence name) {
        throw new UnsupportedOperationException("Cannot get values from post pipelines");
    }

    @Override
    default @Nullable ShaderUniformAccess getUniform(CharSequence name) {
        return null;
    }

    @Override
    default ShaderUniformAccess getUniformSafe(CharSequence name) {
        return ShaderUniformAccess.EMPTY;
    }

    @Override
    default ShaderUniformAccess getOrCreateUniform(CharSequence name) {
        return ShaderUniformAccess.EMPTY;
    }

    @Override
    default int getUniformBlock(CharSequence name) {
        throw new UnsupportedOperationException("Cannot get values from post pipelines");
    }

    @Override
    default int getStorageBlock(CharSequence name) {
        throw new UnsupportedOperationException("Cannot get values from post pipelines");
    }

    @Override
    default boolean hasUniform(CharSequence name) {
        return false;
    }

    @Override
    default boolean hasUniformBlock(CharSequence name) {
        return false;
    }

    @Override
    default boolean hasStorageBlock(CharSequence name) {
        return false;
    }

    @Override
    default void setUniformBlock(CharSequence name, int binding) {
    }

    @Override
    default void setStorageBlock(CharSequence name, int binding) {
    }

    /**
     * Context for applying post pipelines.
     *
     * @author Ocelot
     */
    @ApiStatus.NonExtendable
    interface Context extends ShaderTextureSource.Context {

        /**
         * Binds a named sampler id. All samplers can be applied with {@link #applySamplers(TextureUniformAccess)} for adding them to shaders.
         *
         * @param name      The name of the sampler
         * @param textureId The id of the texture to bind
         * @param samplerId The id of the sampler to bind
         */
        void setSampler(CharSequence name, int textureId, int samplerId);

        /**
         * Sets a framebuffer to a name. This allows post stages to create new framebuffers that can be accessed later on.
         *
         * @param name        The name of the framebuffer
         * @param framebuffer The framebuffer to set
         */
        void setFramebuffer(ResourceLocation name, AdvancedFbo framebuffer);

        /**
         * Applies each sampler to the specified shader.
         *
         * @param shader The shader to apply the samplers to
         */
        void applySamplers(TextureUniformAccess shader);

        /**
         * Removes all post-processing samplers from the specified shader.
         *
         * @param shader The shader to apply the samplers to
         */
        void clearSamplers(TextureUniformAccess shader);

        /**
         * Retrieves a framebuffer by id or the main framebuffer if it doesn't exist.
         *
         * @param name The name of the framebuffer to retrieve
         * @return The framebuffer with that id or the main framebuffer
         */
        default AdvancedFbo getFramebufferOrDraw(ResourceLocation name) {
            AdvancedFbo fbo = this.getFramebuffer(name);
            return fbo != null ? fbo : this.getDrawFramebuffer();
        }

        /**
         * @return The main framebuffer to draw into. This is later copied onto the main framebuffer
         */
        AdvancedFbo getDrawFramebuffer();

        /**
         * Retrieves a post pipeline by name.
         *
         * @param name The name of the pipeline to get
         * @return The registered pipeline or <code>null</code> if it couldn't be found
         */
        default @Nullable PostPipeline getPipeline(ResourceLocation name) {
            return VeilRenderSystem.renderer().getPostProcessingManager().getPipeline(name);
        }

        /**
         * Retrieves a shader by name.
         *
         * @param name The name of the shader to get
         * @return The registered shader or <code>null</code> if it couldn't be found
         */
        default @Nullable ShaderProgram getShader(ResourceLocation name) {
            return VeilRenderSystem.renderer().getShaderManager().getShader(name);
        }
    }
}
