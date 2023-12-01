package foundry.veil.fabric.event;

import foundry.veil.render.pipeline.VeilRenderer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Fired when Veil has finished initialization and the renderer is safe to use.
 *
 * @author Ocelot
 */
@FunctionalInterface
public interface VeilRendererEvent {

    Event<VeilRendererEvent> EVENT = EventFactory.createArrayBacked(VeilRendererEvent.class, events -> renderer -> {
        for (VeilRendererEvent event : events) {
            event.onVeilRendererAvailable(renderer);
        }
    });

    /**
     * Called when the Veil renderer is now available.
     *
     * @param renderer The renderer instance
     */
    void onVeilRendererAvailable(VeilRenderer renderer);
}
