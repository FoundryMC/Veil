package foundry.veil.fabric.event;

import foundry.veil.api.event.VeilRendererEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Fired when Veil has finished initialization and the renderer is safe to use.
 *
 * @author Ocelot
 */
@FunctionalInterface
public interface FabricVeilRendererEvent extends VeilRendererEvent {

    Event<VeilRendererEvent> EVENT = EventFactory.createArrayBacked(VeilRendererEvent.class, events -> renderer -> {
        for (VeilRendererEvent event : events) {
            event.onVeilRendererAvailable(renderer);
        }
    });
}
