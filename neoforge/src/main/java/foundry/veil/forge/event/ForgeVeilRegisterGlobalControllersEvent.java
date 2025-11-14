package foundry.veil.forge.event;

import foundry.veil.api.event.VeilRegisterGlobalControllersEvent;
import foundry.veil.api.flare.modifier.ControllerManager;
import foundry.veil.api.flare.modifier.GlobalController;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired to register global controllers to be used when rendering Flare effects.
 *
 * @author GuyApooye
 * @see ControllerManager
 * @since 2.5.0
 */
public class ForgeVeilRegisterGlobalControllersEvent extends Event implements IModBusEvent {

    private final VeilRegisterGlobalControllersEvent.Registry registry;

    public ForgeVeilRegisterGlobalControllersEvent(VeilRegisterGlobalControllersEvent.Registry registry) {
        this.registry = registry;
    }

    /**
     * Registers the specified global controller to the {@link ControllerManager}.
     *
     * @param globalController The global controller to register.
     */
    public void register(GlobalController globalController) {
        this.registry.registerGlobalController(globalController);
    }
}
