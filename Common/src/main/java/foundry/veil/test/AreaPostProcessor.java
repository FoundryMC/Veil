//package foundry.veil.test;
//
//import foundry.veil.postprocessing.InstantiatedPostProcessor;
//import net.minecraft.resources.ResourceLocation;
//
//public class AreaPostProcessor extends InstantiatedPostProcessor<AreaFx> {
//    ResourceLocation postChainLocation;
//
//    public AreaPostProcessor(ResourceLocation postChainLocation) {
//        this.postChainLocation = postChainLocation;
//    }
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
//    @Override
//    public ResourceLocation getPostChainLocation() {
//        return postChainLocation;
//    }
//
//    @Override
//    public void afterProcess() {
//
//    }
//}
