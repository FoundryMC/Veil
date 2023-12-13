package foundry.veil.platform.services;

import foundry.veil.event.FreeNativeResourcesEvent;
import foundry.veil.event.VeilRendererEvent;

import java.util.ServiceLoader;

/**
 * Manages platform-specific implementations of event subscriptions.
 *
 * @author Ocelot
 */
public interface VeilEventPlatform {

    VeilEventPlatform INSTANCE = ServiceLoader.load(VeilEventPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Failed to find platform event provider"));

    void onFreeNativeResources(FreeNativeResourcesEvent event);

    void onVeilRenderers(VeilRendererEvent event);
}
