package foundry.veil.forge.platform;

import foundry.veil.event.FreeNativeResourcesEvent;
import foundry.veil.event.VeilPostProcessingEvent;
import foundry.veil.event.VeilRendererEvent;
import foundry.veil.forge.event.ForgeFreeNativeResourcesEvent;
import foundry.veil.forge.event.ForgeVeilPostProcessingEvent;
import foundry.veil.forge.event.ForgeVeilRendererEvent;
import foundry.veil.platform.services.VeilEventPlatform;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ForgeVeilEventPlatform implements VeilEventPlatform {

    @Override
    public void onFreeNativeResources(FreeNativeResourcesEvent event) {
        MinecraftForge.EVENT_BUS.<ForgeFreeNativeResourcesEvent>addListener(forgeEvent -> event.onFree());
    }

    @Override
    public void onVeilRenderers(VeilRendererEvent event) {
        MinecraftForge.EVENT_BUS.<ForgeVeilRendererEvent>addListener(forgeEvent -> event.onVeilRendererAvailable(forgeEvent.getRenderer()));
    }

    @Override
    public void preVeilPostProcessing(VeilPostProcessingEvent.Pre event) {
        MinecraftForge.EVENT_BUS.<ForgeVeilPostProcessingEvent.Pre>addListener(forgeEvent -> event.preVeilPostProcessing(forgeEvent.getName(), forgeEvent.getPipeline()));
    }

    @Override
    public void postVeilPostProcessing(VeilPostProcessingEvent.Post event) {
        MinecraftForge.EVENT_BUS.<ForgeVeilPostProcessingEvent.Post>addListener(forgeEvent -> event.postVeilPostProcessing(forgeEvent.getName(), forgeEvent.getPipeline()));
    }
}
