//package foundry.veil.postprocessing;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import foundry.veil.Veil;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class PostProcessingHandler {
//    private static final List<PostProcessor> instances = new ArrayList<>();
//
//    private static boolean copiedDepth = false;
//
//    /**
//     * Add a {@link PostProcessor} instance to the handler.
//     * IMPORTANT: PostProcessors must be added in the correct order (e.g. bloom must be added after the depth buffer is copied)
//     */
//    public static void addInstance(PostProcessor instance) {
//        instances.add(instance);
//    }
//
//    public static void copyDepth(){
//        if(copiedDepth) return;
//        instances.forEach(PostProcessor::copyDepthBuffer);
//        copiedDepth = true;
//    }
//
//    public static void resize(int width, int height){
//        instances.forEach(instance -> instance.resize(width, height));
//    }
//
//    public static void onLevelRenderLast(PoseStack stack){
//        copyDepth();
//        PostProcessor.viewModelStack = stack;
//        instances.forEach(PostProcessor::applyPostProcess);
//        copiedDepth = true;
//    }
//
//    public static void stopEffect(PostProcessor instance){
//        instance.setActive(false);
//    }
//
//    public static List<PostProcessor> getInstances() {
//        return instances;
//    }
//}
