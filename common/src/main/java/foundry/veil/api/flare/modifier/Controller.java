package foundry.veil.api.flare.modifier;

import foundry.veil.api.client.property.Property;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.data.FloatCurve;

/**
 * Controllers collect values from {@link EffectHost}s to be used to evaluate {@link FloatCurve}s and modify {@link Property}s.
 *
 * @author GuyApooye
 * @since 2.5.0
 */
public class Controller {

    protected final ControllerIdentifier identifier;
    protected final EffectHost host;
    protected float value;

    public Controller(String name, EffectHost host) {
        this(new ControllerIdentifier(name, host), host);
    }

    public Controller(ControllerIdentifier identifier, EffectHost host) {
        this.identifier = identifier;
        this.host = host;
    }

    protected void initialize() {
        this.update(0.0f);
    }

    public void update(float partialTick) {
        this.host.update(partialTick);
        this.value = this.getUpdatedValue();
    }

    protected float getUpdatedValue() {
        return this.host.getValue(this.identifier.name());
    }

    public float getValue() {
        return this.value;
    }

    public ControllerIdentifier getIdentifier() {
        return this.identifier;
    }
}
