package foundry.veil.api.flare.data.effect;

import com.mojang.serialization.Codec;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.flare.model.BakedShell;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.resources.ResourceLocation;
import foundry.veil.api.flare.EffectHost;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class FlareEffectTemplate {
    public static final Codec<FlareEffectTemplate> CODEC = CodecUtil.singleOrList(FlareEffectLayer.CODEC).fieldOf("layers").xmap(FlareEffectTemplate::new, FlareEffectTemplate::effectLayers).codec();
    private final List<FlareEffectLayer> effectLayers;

    public FlareEffectTemplate(List<FlareEffectLayer> effectLayers) {
        List<FlareEffectLayer> disabledLayers = new ArrayList<>();
        List<FlareEffectLayer> enabledLayers = new ArrayList<>(effectLayers);

        for (FlareEffectLayer effectLayer : effectLayers) {
            if (effectLayer.isDisabled()) disabledLayers.add(effectLayer);
        }

        for (FlareEffectLayer disabledLayer : disabledLayers) {
            enabledLayers.remove(disabledLayer);
        }

        this.effectLayers = Collections.unmodifiableList(enabledLayers);
    }

    public void render(EffectHost host, MatrixStack matrixStack, float partialTick, @Nullable Map<ResourceLocation, BakedShell> shellOverrides) {

        for (int i = 0, effectLayersSize = effectLayers.size(); i < effectLayersSize; i++) {
            FlareEffectLayer effectLayer = effectLayers.get(i);
            effectLayer.render(host, matrixStack, partialTick, shellOverrides);
        }

    }

    public List<FlareEffectLayer> effectLayers() {
        return effectLayers;
    }
}
