package foundry.veil;

import foundry.veil.postprocessing.PostProcessingHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class VeilFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VeilClient.init();
        WorldRenderEvents.LAST.register(last -> {
            PostProcessingHandler.onLevelRenderLast(last.matrixStack());
        });
    }
}
