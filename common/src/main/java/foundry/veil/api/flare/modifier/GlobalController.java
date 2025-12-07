package foundry.veil.api.flare.modifier;

/**
 * A controller that collects its value locally.
 *
 * @author GuyApooye
 * @see RandomnessController
 * @since 2.5.0
 */
public abstract non-sealed class GlobalController implements Controller {

    private final String name;
    
    public GlobalController(String name) {
        this.name = "global::" + name;
    }
    
    @Override
    public void update(float partialTick) {
    }
    
    @Override
    public float getValue() {
        return this.getUpdatedValue();
    }
    
    @Override
    public String getName() {
        return name;
    }
}
