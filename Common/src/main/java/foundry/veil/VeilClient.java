package foundry.veil;

import foundry.veil.color.Color;
import foundry.veil.optimization.OptimizationUtil;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.shader.RenderTypeRegistry;

public class VeilClient {

    public static void init() {
        RenderTypeRegistry.init();
    }

    public static void initRenderer() {
        VeilRenderSystem.init();
    }

    public static void tickClient(int ticks, float partialTick) {
        Color.tickRainbow(ticks, partialTick);
        if(ticks % 200 == 0){
            OptimizationUtil.calculateStableFps();
        }
    }
}
