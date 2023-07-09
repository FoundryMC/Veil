//package foundry.veil.test;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import foundry.veil.Veil;
//import foundry.veil.postprocessing.InstantiatedPostProcessor;
//import net.minecraft.client.renderer.EffectInstance;
//import net.minecraft.resources.ResourceLocation;
//
//public class OutlinePostProcessor extends InstantiatedPostProcessor<OutlineFx> {
//    private EffectInstance effectBloom;
//
//    @Override
//    public ResourceLocation getPostChainLocation() {
//        return Veil.veilPath("outline");
//    }
//
//    @Override
//    protected int getMaxInstances() {
//        return 16;
//    }
//
//    @Override
//    protected int getDataSizePerInstance() {
//        return 19;
//    }
//
//    public EffectInstance getEffectBloom() {
//        return effectBloom;
//    }
//
//    @Override
//    public void init() {
//        super.init();
//
//        if (postChain != null)
//            effectBloom = effects[0];
//    }
//
//    @Override
//    public void beforeProcess(PoseStack viewModelStack) {
//        super.beforeProcess(viewModelStack);
//
//        //setDataBufferUniform(effectBloom, "Data", "instanceCount");
//    }
//
//    @Override
//    public void afterProcess() {
//
//    }
//}
