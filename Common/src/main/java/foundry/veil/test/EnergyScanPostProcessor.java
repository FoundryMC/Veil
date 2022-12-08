package foundry.veil.test;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.Veil;
import foundry.veil.postprocessing.InstantiatedPostProcessor;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;

public class EnergyScanPostProcessor extends InstantiatedPostProcessor<EnergyScanFx> {
    private EffectInstance effectEnergyScan;

    @Override
    public ResourceLocation getPostChainLocation() {
        return Veil.veilPath("energy_scan");
    }

    @Override
    protected int getMaxInstances() {
        return 16;
    }

    @Override
    protected int getDataSizePerInstance() {
        return 19;
    }

    @Override
    public void init() {
        super.init();

        if (postChain != null)
            effectEnergyScan = effects[0];
    }

    @Override
    public void beforeProcess(PoseStack viewModelStack) {
        super.beforeProcess(viewModelStack);

        setDataBufferUniform(effectEnergyScan, "Data", "instanceCount");
    }

    @Override
    public void afterProcess() {

    }
}
