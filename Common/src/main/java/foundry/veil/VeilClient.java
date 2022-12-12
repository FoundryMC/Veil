package foundry.veil;

import foundry.veil.shader.RenderTypeRegistry;
import foundry.veil.test.PostProcessingEffectsRegistry;

public class VeilClient {
    public static void init(){
        PostProcessingEffectsRegistry.init();
        RenderTypeRegistry.init();
    }
}
