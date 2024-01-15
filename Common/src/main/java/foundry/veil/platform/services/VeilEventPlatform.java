package foundry.veil.platform.services;

import foundry.veil.api.event.FreeNativeResourcesEvent;
import foundry.veil.api.event.VeilPostProcessingEvent;
import foundry.veil.api.event.VeilRendererEvent;

import java.util.ServiceLoader;

/**
 * Manages platform-specific implementations of event subscriptions.
 *
 * @author Ocelot
 */
public interface VeilEventPlatform {

    VeilEventPlatform INSTANCE = ServiceLoader.load(VeilEventPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Failed to find platform event provider"));

    void onFreeNativeResources(FreeNativeResourcesEvent event);

    void onVeilRendererAvailable(VeilRendererEvent event);

    void preVeilPostProcessing(VeilPostProcessingEvent.Pre event);

    void postVeilPostProcessing(VeilPostProcessingEvent.Post event);
}
