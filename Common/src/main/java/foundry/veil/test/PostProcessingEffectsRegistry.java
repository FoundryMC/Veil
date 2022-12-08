package foundry.veil.test;

import foundry.veil.postprocessing.PostProcessingHandler;

public class PostProcessingEffectsRegistry {
    public static final EnergySpherePostProcessor ENERGY_SPHERE = new EnergySpherePostProcessor();
    public static final EnergyScanPostProcessor ENERGY_SCAN = new EnergyScanPostProcessor();

    public static void init() {
        PostProcessingHandler.addInstance(ENERGY_SPHERE);
        PostProcessingHandler.addInstance(ENERGY_SCAN);
    }
}
