package foundry.veil.render.post.stage;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.render.post.PostPipeline;
import foundry.veil.render.post.PostPipelineStageRegistry;

/**
 * Sets the color and depth masks.
 *
 * @param red   Whether red values will be written to the screen
 * @param green Whether green values will be written to the screen
 * @param blue  Whether blue values will be written to the screen
 * @param alpha Whether alpha values will be written to the screen
 * @param depth Whether depth values will be written to the screen
 * @author Ocelot
 */
public record MaskPostStage(boolean red,
                            boolean green,
                            boolean blue,
                            boolean alpha,
                            boolean depth) implements PostPipeline {

    public static final Codec<MaskPostStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("red", false).forGetter(MaskPostStage::red),
            Codec.BOOL.optionalFieldOf("green", false).forGetter(MaskPostStage::green),
            Codec.BOOL.optionalFieldOf("blue", false).forGetter(MaskPostStage::blue),
            Codec.BOOL.optionalFieldOf("alpha", false).forGetter(MaskPostStage::alpha),
            Codec.BOOL.optionalFieldOf("depth", false).forGetter(MaskPostStage::depth)
    ).apply(instance, MaskPostStage::new));

    @Override
    public void apply(PostPipeline.Context context) {
        RenderSystem.colorMask(this.red, this.green, this.blue, this.alpha);
        RenderSystem.depthMask(this.depth);
    }

    @Override
    public PostPipelineStageRegistry.PipelineType<? extends PostPipeline> getType() {
        return PostPipelineStageRegistry.MASK;
    }
}
