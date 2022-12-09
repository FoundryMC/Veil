package foundry.veil.test;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.Veil;
import foundry.veil.postprocessing.InstantiatedPostProcessor;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;

public class BloomPostProcessor extends InstantiatedPostProcessor<BloomFx> {
    private EffectInstance effectBloom;

    @Override
    public ResourceLocation getPostChainLocation() {
        return Veil.veilPath("bloom");
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
            effectBloom = effects[0];
    }

    @Override
    public void beforeProcess(PoseStack viewModelStack) {
        super.beforeProcess(viewModelStack);

        //setDataBufferUniform(effectBloom, "Data", "instanceCount");
    }

    @Override
    public void afterProcess() {

    }
}
