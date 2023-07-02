package foundry.veil.render.post;

import com.mojang.serialization.Codec;
import foundry.veil.render.framebuffer.AdvancedFbo;
import foundry.veil.render.shader.program.ShaderProgram;
import foundry.veil.render.shader.texture.ShaderTextureSource;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

/**
 * <p>A series of post-processing effects that can be run to change the current framebuffer state.</p>
 * <p>It can be fully run using {@link PostProcessingManager#runPipeline(PostPipeline)}</p>
 *
 * @author Ocelot
 */
public interface PostPipeline extends NativeResource {

    Codec<PostPipeline> CODEC = PostPipelineStageRegistry.CODEC.dispatch(PostPipeline::getType, PostPipelineStageRegistry.PipelineType::codec);

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

    /**
     * Context for applying post pipelines.
     *
     * @author Ocelot
     */
    interface Context extends RenderContext, ShaderTextureSource.Context {

        /**
         * Binds a named sampler id. All samplers can be applied with {@link #applySamplers(ShaderProgram)} for adding them to shaders.
         *
         * @param name The name of the sampler
         * @param id   The id of the texture to bind
         */
        void setSampler(CharSequence name, int id);

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
        void applySamplers(ShaderProgram shader);

        /**
         * Retrieves a framebuffer by id or the main framebuffer if it doesn't exist.
         *
         * @param name The name of the framebuffer to retrieve
         * @return The framebuffer with that id or the main framebuffer
         */
        default AdvancedFbo getFramebufferOrMain(ResourceLocation name) {
            AdvancedFbo fbo = this.getFramebuffer(name);
            return fbo != null ? fbo : this.getDrawFramebuffer();
        }

        /**
         * @return The main framebuffer to draw into. This is later copied onto the main framebuffer
         */
        AdvancedFbo getDrawFramebuffer();

        @Override
        default AbstractTexture getTexture(ResourceLocation name) {
            return this.getTextureManager().getTexture(name);
        }
    }
}
