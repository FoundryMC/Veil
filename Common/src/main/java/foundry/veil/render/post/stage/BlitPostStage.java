package foundry.veil.render.post.stage;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.render.framebuffer.FramebufferManager;
import foundry.veil.render.post.PostPipeline;
import foundry.veil.render.post.PostPipelineStageRegistry;
import foundry.veil.render.shader.ShaderProgram;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;

/**
 * A basic stage that draws a quad to the output using a specified shader.
 *
 * @author Ocelot
 */
public class BlitPostStage extends FramebufferPostStage {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Codec<BlitPostStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("shader").forGetter(BlitPostStage::getShader),
            FramebufferManager.FRAMEBUFFER_CODEC.optionalFieldOf("in").forGetter(stage -> Optional.ofNullable(stage.getIn())),
            FramebufferManager.FRAMEBUFFER_CODEC.fieldOf("out").forGetter(BlitPostStage::getOut),
            Codec.BOOL.optionalFieldOf("clear", true).forGetter(BlitPostStage::clearOut)
    ).apply(instance, (shader, in, out, clear) -> new BlitPostStage(shader, in.orElse(null), out, clear)));

    private final ResourceLocation shader;

    /**
     * Creates a new blit post stage that applies the specified shader.
     *
     * @param shader The shader to apply
     * @param in     The framebuffer to use as <code>DiffuseSampler0</code>-<code>DiffuseSampler7</code>
     *               and <code>DiffuseDepthSampler</code>
     * @param out    The framebuffer to write into
     * @param clear  Whether to clear the output before drawing
     */
    public BlitPostStage(ResourceLocation shader, @Nullable ResourceLocation in, ResourceLocation out, boolean clear) {
        super(in, out, clear);
        this.shader = Objects.requireNonNull(shader, "shader");
    }

    @Override
    public void apply(Context context) {
        ShaderProgram shader = context.getShaderManager().getShader(this.shader);
        if (shader == null) {
            LOGGER.warn("Failed to find post shader: " + this.shader);
            return;
        }

        shader.bind();
        shader.applyRenderSystem();
        float[] color = RenderSystem.getShaderColor();
        shader.setVector("ColorModulator", color[0], color[1], color[2], color[3]);
        context.applySamplers(shader);
        this.setupFramebuffer(context, shader);
        shader.applyShaderSamplers(context, 0);
        context.drawScreenQuad();
    }

    @Override
    public PostPipelineStageRegistry.PipelineType<? extends PostPipeline> getType() {
        return PostPipelineStageRegistry.BLIT;
    }

    /**
     * @return The shader this stage should use
     */
    public ResourceLocation getShader() {
        return this.shader;
    }
}
