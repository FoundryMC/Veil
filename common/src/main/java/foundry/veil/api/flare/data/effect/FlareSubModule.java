package foundry.veil.api.flare.data.effect;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.FlareEffectManager;
import foundry.veil.api.flare.model.BakedShell;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static foundry.veil.Veil.LOGGER;

/**
 * @since 2.5.0
 */
public final class FlareSubModule {
    
    public static final Codec<FlareSubModule> CODEC = Codec.either(
                    ResourceLocation.CODEC.listOf(),
                    ResourceLocation.CODEC
            )
            .xmap(either -> either.map(FlareSubModule::new, single -> new FlareSubModule(List.of(single))),
                    subModule -> subModule.templates.size() == 1 ? Either.right(subModule.templates.getFirst()) : Either.left(subModule.templates));
    private final List<ResourceLocation> templates;
    
    
    public FlareSubModule(List<ResourceLocation> templates) {
        this.templates = List.copyOf(templates);
    }
    
    public void render(EffectHost host, MatrixStack matrixStack, float partialTick) {
        this.render(host, matrixStack, partialTick, null);
    }
    
    public void render(EffectHost host, MatrixStack matrixStack, float partialTick, @Nullable Map<ResourceLocation, BakedShell> shellOverrides) {
        List<ResourceLocation> resourceLocations = this.templates;
        for (int i = 0, size = resourceLocations.size(); i < size; i++) {
            ResourceLocation templateLocation = resourceLocations.get(i);
            FlareEffectTemplate template = FlareEffectManager.getTemplate(templateLocation);
            if (template == null) {
                LOGGER.error("Template {} could not be found!", templateLocation);
                continue;
            }
            template.render(host, matrixStack, partialTick, shellOverrides);
        }
    }
    
    public List<ResourceLocation> templates() {
        return templates;
    }
    
}
