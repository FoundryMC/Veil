package foundry.veil.api.flare.data.effect;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.flare.model.BakedShell;
import net.minecraft.resources.ResourceLocation;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.FlareEffectManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static foundry.veil.Veil.LOGGER;

public final class FlareSubModule {
    public static final Codec<FlareSubModule> CODEC = Codec.either(ResourceLocation.CODEC.listOf()
                            .xmap(FlareSubModule::new, FlareSubModule::templates),
                    ResourceLocation.CODEC.xmap(List::of, List::getFirst)
            )
            .xmap(FlareSubModule::eitherToModule, FlareSubModule::moduleToEither);
    private final List<ResourceLocation> templates;

    public FlareSubModule(List<ResourceLocation> templates) {
        this.templates = templates;
    }

    private static Either<FlareSubModule, List<ResourceLocation>> moduleToEither(FlareSubModule subModule) {
        return Either.right(subModule.templates);
    }

    public void render(EffectHost host, MatrixStack matrixStack, float partialTick, @Nullable Map<ResourceLocation, BakedShell> shellOverrides) {
        FlareEffectManager flare = FlareEffectManager.getInstance();
        for (int i = 0, templatesSize = templates.size(); i < templatesSize; i++) {
            ResourceLocation templateLocation = templates.get(i);
            FlareEffectTemplate template = flare.getTemplate(templateLocation);
            if (template == null) {
                LOGGER.error("Template {} could not be found!", templateLocation);
                continue;
            }
            template.render(host, matrixStack, partialTick, shellOverrides);
        }
    }

    private static FlareSubModule eitherToModule(Either<FlareSubModule, List<ResourceLocation>> either) {
        return either.map(Function.identity(), FlareSubModule::new);
    }

    private List<ResourceLocation> templates() {
        return templates;
    }

}
