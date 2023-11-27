package foundry.veil.fabric.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Fired when Minecraft frees all native resources on the client.
 *
 * @author Ocelot
 */
@FunctionalInterface
public interface FreeNativeResourcesEvent {

    Event<FreeNativeResourcesEvent> EVENT = EventFactory.createArrayBacked(FreeNativeResourcesEvent.class, events -> () -> {
        for (FreeNativeResourcesEvent event : events) {
            event.onFree();
        }
    });

    /**
     * Called after all Minecraft native resources have been freed, but before the executors have been shut down.
     */
    void onFree();
}
