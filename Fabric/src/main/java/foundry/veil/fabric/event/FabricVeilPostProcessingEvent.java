package foundry.veil.fabric.event;

import foundry.veil.event.VeilPostProcessingEvent;
import foundry.veil.render.post.PostProcessingManager;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * <p>Events fired when Veil runs post-processing.</p>
 *
 * <p><b><i>Note: These events are only fired if there are post-processing steps to run.</i></b></p>
 *
 * @author Ocelot
 * @see PostProcessingManager
 */
public final class FabricVeilPostProcessingEvent {

    public static final Event<VeilPostProcessingEvent.Pre> PRE = EventFactory.createArrayBacked(VeilPostProcessingEvent.Pre.class, events -> (name, pipeline) -> {
        for (VeilPostProcessingEvent.Pre event : events) {
            event.preVeilPostProcessing(name, pipeline);
        }
    });

    public static final Event<VeilPostProcessingEvent.Post> POST = EventFactory.createArrayBacked(VeilPostProcessingEvent.Post.class, events -> (name, pipeline) -> {
        for (VeilPostProcessingEvent.Post event : events) {
            event.postVeilPostProcessing(name, pipeline);
        }
    });

    private FabricVeilPostProcessingEvent() {
    }
}
