package foundry.veil.fabric.event;

import foundry.veil.event.FreeNativeResourcesEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Fired when Minecraft frees all native resources on the client.
 *
 * @author Ocelot
 */
@FunctionalInterface
public interface FabricFreeNativeResourcesEvent extends FreeNativeResourcesEvent {

    Event<FreeNativeResourcesEvent> EVENT = EventFactory.createArrayBacked(FreeNativeResourcesEvent.class, events -> () -> {
        for (FreeNativeResourcesEvent event : events) {
            event.onFree();
        }
    });
}
