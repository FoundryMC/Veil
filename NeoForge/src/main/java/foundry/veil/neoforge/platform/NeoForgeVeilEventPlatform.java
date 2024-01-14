package foundry.veil.neoforge.platform;

import foundry.veil.event.FreeNativeResourcesEvent;
import foundry.veil.event.VeilPostProcessingEvent;
import foundry.veil.event.VeilRendererEvent;
import foundry.veil.neoforge.event.NeoForgeFreeNativeResourcesEvent;
import foundry.veil.neoforge.event.NeoForgeVeilPostProcessingEvent;
import foundry.veil.neoforge.event.NeoForgeVeilRendererEvent;
import foundry.veil.platform.services.VeilEventPlatform;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NeoForgeVeilEventPlatform implements VeilEventPlatform {

    @Override
    public void onFreeNativeResources(FreeNativeResourcesEvent event) {
        NeoForge.EVENT_BUS.addListener(NeoForgeFreeNativeResourcesEvent.class, forgeEvent -> event.onFree());
    }

    @Override
    public void onVeilRendererAvailable(VeilRendererEvent event) {
        NeoForge.EVENT_BUS.addListener(NeoForgeVeilRendererEvent.class, forgeEvent -> event.onVeilRendererAvailable(forgeEvent.getRenderer()));
    }

    @Override
    public void preVeilPostProcessing(VeilPostProcessingEvent.Pre event) {
        NeoForge.EVENT_BUS.addListener(NeoForgeVeilPostProcessingEvent.Pre.class, forgeEvent -> event.preVeilPostProcessing(forgeEvent.getName(), forgeEvent.getPipeline(), forgeEvent.getContext()));
    }

    @Override
    public void postVeilPostProcessing(VeilPostProcessingEvent.Post event) {
        NeoForge.EVENT_BUS.addListener(NeoForgeVeilPostProcessingEvent.Post.class, forgeEvent -> event.postVeilPostProcessing(forgeEvent.getName(), forgeEvent.getPipeline(), forgeEvent.getContext()));
    }
}
