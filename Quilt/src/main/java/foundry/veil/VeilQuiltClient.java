package foundry.veil;

import foundry.veil.postprocessing.PostProcessingHandler;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class VeilQuiltClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        VeilClient.init();
        WorldRenderEvents.LAST.register(last -> {
            PostProcessingHandler.onLevelRenderLast(last.matrixStack());
        });
    }
}
