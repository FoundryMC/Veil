package foundry.veil.fabric.event;

import foundry.veil.api.event.VeilRegisterGlobalControllersEvent;
import foundry.veil.api.flare.modifier.ControllerManager;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Fired to register global controllers to be used when rendering Flare effects.
 *
 * @author GuyApooye
 * @see ControllerManager
 * @since 2.5.0
 */
@FunctionalInterface
public interface FabricVeilRegisterGlobalControllersEvent extends VeilRegisterGlobalControllersEvent {
    Event<VeilRegisterGlobalControllersEvent> EVENT = EventFactory.createArrayBacked(VeilRegisterGlobalControllersEvent.class, events -> registry -> {
        for (VeilRegisterGlobalControllersEvent event : events) {
            event.onRegisterGlobalControllers(registry);
        }
    });
}
