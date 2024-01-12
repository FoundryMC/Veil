package foundry.veil.fabric.platform;

import foundry.veil.event.FreeNativeResourcesEvent;
import foundry.veil.event.VeilPostProcessingEvent;
import foundry.veil.event.VeilRendererEvent;
import foundry.veil.fabric.event.FabricFreeNativeResourcesEvent;
import foundry.veil.fabric.event.FabricVeilPostProcessingEvent;
import foundry.veil.fabric.event.FabricVeilRendererEvent;
import foundry.veil.platform.services.VeilEventPlatform;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class FabricVeilEventPlatform implements VeilEventPlatform {

    @Override
    public void onFreeNativeResources(FreeNativeResourcesEvent event) {
        FabricFreeNativeResourcesEvent.EVENT.register(event);
    }

    @Override
    public void onVeilRendererAvailable(VeilRendererEvent event) {
        FabricVeilRendererEvent.EVENT.register(event);
    }

    @Override
    public void preVeilPostProcessing(VeilPostProcessingEvent.Pre event) {
        FabricVeilPostProcessingEvent.PRE.register(event);
    }

    @Override
    public void postVeilPostProcessing(VeilPostProcessingEvent.Post event) {
        FabricVeilPostProcessingEvent.POST.register(event);
    }
}
