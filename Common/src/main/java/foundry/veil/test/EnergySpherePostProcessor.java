package foundry.veil.test;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.Veil;
import foundry.veil.postprocessing.InstantiatedPostProcessor;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;

public class EnergySpherePostProcessor extends InstantiatedPostProcessor<EnergySphereFx> {
    private EffectInstance effectEnergySphere;

    @Override
    public ResourceLocation getPostChainLocation() {
        return Veil.veilPath("energy_sphere");
    }

    @Override
    protected int getMaxInstances() {
        return 16;
    }

    @Override
    protected int getDataSizePerInstance() {
        return 8;
    }

    @Override
    public void init() {
        super.init();

        if (postChain != null)
            effectEnergySphere = effects[0];
    }

    @Override
    public void beforeProcess(PoseStack viewModelStack) {
        super.beforeProcess(viewModelStack);

        setDataBufferUniform(effectEnergySphere, "Data", "instanceCount");
    }

    @Override
    public void afterProcess() {

    }
}
