package foundry.veil.api.flare.modifier;

import foundry.veil.api.flare.EffectHost;
import org.jetbrains.annotations.NotNull;

/**
 * @since 2.5.0
 */
public record ControllerIdentifier(String name, String host) {

    public ControllerIdentifier(String name, EffectHost host) {
        this(name, host.getName());
    }

    @Override
    public @NotNull String toString() {
        return this.name + "$" + this.host;
    }
}
