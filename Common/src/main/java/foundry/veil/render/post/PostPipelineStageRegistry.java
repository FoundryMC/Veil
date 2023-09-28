package foundry.veil.render.post;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import foundry.veil.Veil;
import foundry.veil.render.post.stage.BlitPostStage;
import foundry.veil.render.post.stage.CopyPostStage;
import foundry.veil.render.post.stage.MaskPostStage;
import net.minecraft.resources.ResourceLocation;

// TODO use registry
/**
 * Registry for all post pipeline stages.
 */
public class PostPipelineStageRegistry {

    private static final BiMap<ResourceLocation, PipelineType<?>> EFFECT_TYPES = HashBiMap.create();
    public static final Codec<PipelineType<?>> CODEC = ResourceLocation.CODEC.flatXmap(location -> {
        PipelineType<?> pipelineType = EFFECT_TYPES.get(location);
        if (pipelineType != null) {
            return DataResult.success(pipelineType);
        }
        return DataResult.error(() -> "Unknown post pipeline type " + location);
    }, pipelineType -> {
        ResourceLocation location = EFFECT_TYPES.inverse().get(pipelineType);
        if (pipelineType != null) {
            return DataResult.success(location);
        }
        return DataResult.error(() -> "Unknown post pipeline type " + location);
    });

    public static final PipelineType<BlitPostStage> BLIT = register("blit", BlitPostStage.CODEC);
    public static final PipelineType<CopyPostStage> COPY = register("copy", CopyPostStage.CODEC);
    public static final PipelineType<MaskPostStage> MASK = register("mask", MaskPostStage.CODEC);

    private static <T extends PostPipeline> PipelineType<T> register(String name, Codec<T> codec) {
        PipelineType<T> pipelineType = new PipelineType<>(codec);
        ResourceLocation location = new ResourceLocation(Veil.MODID, name);
        if (PostPipelineStageRegistry.EFFECT_TYPES.putIfAbsent(location, pipelineType) != null) {
            throw new IllegalStateException("Duplicate pipeline type type registration " + location);
        }

        return pipelineType;
    }

    public record PipelineType<T extends PostPipeline>(Codec<T> codec) {
    }
}
