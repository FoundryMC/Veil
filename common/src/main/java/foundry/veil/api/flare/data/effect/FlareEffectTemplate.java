package foundry.veil.api.flare.data.effect;

import com.mojang.serialization.Codec;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.model.BakedShell;
import foundry.veil.api.util.CodecUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since 2.5.0
 */
public record FlareEffectTemplate(List<FlareEffectLayer> effectLayers) {

    public static final Codec<FlareEffectTemplate> CODEC = CodecUtil.singleOrList(FlareEffectLayer.CODEC).fieldOf("layers").xmap(FlareEffectTemplate::new, FlareEffectTemplate::effectLayers).codec();

    public FlareEffectTemplate(List<FlareEffectLayer> effectLayers) {
        ArrayList<FlareEffectLayer> enabledLayers = new ArrayList<>(effectLayers.size());

        for (FlareEffectLayer effectLayer : effectLayers) {
            if (!effectLayer.isDisabled()) {
                enabledLayers.add(effectLayer);
            }
        }

        this.effectLayers = Collections.unmodifiableList(new ObjectArrayList<>(enabledLayers));
    }

    public void render(EffectHost host, MatrixStack matrixStack, float partialTick, @Nullable Map<ResourceLocation, BakedShell> shellOverrides) {
        for (FlareEffectLayer effectLayer : this.effectLayers) {
            effectLayer.render(host, matrixStack, partialTick, shellOverrides);
        }
    }

    public void render(EffectHost host, MatrixStack matrixStack, float partialTick) {
        this.render(host, matrixStack, partialTick, null);
    }
}
