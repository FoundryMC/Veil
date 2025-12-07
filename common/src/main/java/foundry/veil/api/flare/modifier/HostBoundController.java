package foundry.veil.api.flare.modifier;

import foundry.veil.api.flare.EffectHost;

/**
 * A controller that collects values from {@link EffectHost EffectHosts}.
 *
 * @author GuyApooye
 * @since 2.5.0
 */
public non-sealed class HostBoundController implements Controller {

    private final String name;
    private final String hostName;
    protected final EffectHost host;
    protected float value;

    public HostBoundController(String name, EffectHost host) {
        this.name = name;
        this.hostName = host.getName();
        this.host = host;
    }

    @Override
    public void initialize() {
        this.update(0.0f);
    }

    @Override
    public void update(float partialTick) {
        this.host.update(partialTick);
        this.value = this.getUpdatedValue();
    }

    @Override
    public float getUpdatedValue() {
        return this.host.getValue(this.name);
    }

    @Override
    public float getValue() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }
    
    public String getHost() {
        return this.hostName;
    }
}
