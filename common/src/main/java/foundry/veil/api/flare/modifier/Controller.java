package foundry.veil.api.flare.modifier;

import foundry.veil.api.client.property.Property;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.data.FloatCurve;

/**
 * Controllers collect values from {@link EffectHost}s to be used to evaluate {@link FloatCurve}s and modify {@link Property}s.
 *
 * @author GuyApooye
 */
public class Controller {
    protected final ControllerIdentifier identifier;
    protected final EffectHost host;
    protected float value;

    public Controller(String name, EffectHost host) {
        this.identifier = new ControllerIdentifier(name, host);
        this.host = host;
    }

    public Controller(ControllerIdentifier identifier, EffectHost host) {
        this.identifier = identifier;
        this.host = host;
    }

    protected void initialize() {
        update(0.0f);
    }

    public void update(float partialTick) {
        host.update(partialTick);
        value = getUpdatedValue();
    }

    protected float getUpdatedValue() {
        return host.getValue(identifier.name());
    }

    public float getValue() {
        return value;
    }

    public ControllerIdentifier getIdentifier() {
        return identifier;
    }
}
