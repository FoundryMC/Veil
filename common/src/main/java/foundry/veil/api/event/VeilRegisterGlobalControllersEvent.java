package foundry.veil.api.event;

import foundry.veil.api.flare.modifier.ControllerManager;
import foundry.veil.api.flare.modifier.GlobalController;

/**
 * Fired to register global controllers to be used when rendering Flare effects.
 *
 * @author GuyApooye
 * @since 2.5.0
 */
@FunctionalInterface
public interface VeilRegisterGlobalControllersEvent {

    /**
     * Registers global controllers.
     *
     * @param registry The registry to add global controllers to
     */
    void onRegisterGlobalControllers(Registry registry);

    /**
     * Registers global controllers.
     *
     * @author GuyApooye
     */
    @FunctionalInterface
    interface Registry {

        /**
         * Registers the specified global controller to the {@link ControllerManager}.
         *
         * @param globalController The global controller to register.
         */
        void registerGlobalController(GlobalController globalController);
    }
}
