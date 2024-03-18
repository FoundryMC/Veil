package foundry.veil.api.client.registry;

import com.mojang.serialization.Codec;
import foundry.veil.Veil;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.post.stage.BlitPostStage;
import foundry.veil.api.client.render.post.stage.CopyPostStage;
import foundry.veil.api.client.render.post.stage.MaskPostStage;
import foundry.veil.platform.registry.RegistrationProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

/**
 * Registry for all post pipeline stages.
 */
public class PostPipelineStageRegistry {

    public static final ResourceKey<Registry<PostPipelineStageRegistry.PipelineType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(Veil.veilPath("post_pipeline_stage"));
    private static final RegistrationProvider<PostPipelineStageRegistry.PipelineType<?>> PROVIDER = RegistrationProvider.get(REGISTRY_KEY, Veil.MODID);
    public static final Registry<PostPipelineStageRegistry.PipelineType<?>> REGISTRY = PROVIDER.asVanillaRegistry();

    public static final Supplier<PipelineType<BlitPostStage>> BLIT = register("blit", BlitPostStage.CODEC);
    public static final Supplier<PipelineType<CopyPostStage>> COPY = register("copy", CopyPostStage.CODEC);
    public static final Supplier<PipelineType<MaskPostStage>> MASK = register("mask", MaskPostStage.CODEC);

    @ApiStatus.Internal
    public static void bootstrap() {
    }

    private static <T extends PostPipeline> Supplier<PipelineType<T>> register(String name, Codec<T> codec) {
        return PROVIDER.register(name, () -> new PipelineType<>(codec));
    }

    public record PipelineType<T extends PostPipeline>(Codec<T> codec) {
    }
}
