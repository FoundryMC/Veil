package foundry.veil;

import foundry.veil.color.Color;
import foundry.veil.shader.RenderTypeRegistry;
import foundry.veil.test.PostProcessingEffectsRegistry;

public class VeilClient {
    public static void init(){
        PostProcessingEffectsRegistry.init();
        RenderTypeRegistry.init();
    }

    public static void tickClient(int ticks, float partialTick){
        Color.tickRainbow(ticks, partialTick);
    }
}
