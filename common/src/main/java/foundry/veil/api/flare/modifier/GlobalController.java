package foundry.veil.api.flare.modifier;

/**
 * Global controller have no host and collect their value locally.
 *
 * @see RandomnessController
 * @author GuyApooye
 */
public abstract class GlobalController extends Controller {

    public GlobalController(String name) {
        super(new ControllerIdentifier("global::" + name, "global"), null);
    }

}
