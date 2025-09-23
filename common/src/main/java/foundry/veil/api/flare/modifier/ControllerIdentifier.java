package foundry.veil.api.flare.modifier;

import foundry.veil.api.flare.EffectHost;

import java.util.Objects;

public class ControllerIdentifier {
    private final String name;
    private final String host;

    public ControllerIdentifier(String name, EffectHost host) {
        this.name = name;
        this.host = host.getName();
    }

    public ControllerIdentifier(String name, String host) {
        this.name = name;
        this.host = host;
    }

    public String name() {
        return name;
    }

    public String host() {
        return host;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ControllerIdentifier other = (ControllerIdentifier) obj;
        return this.name.equals(other.name) &&
                this.host.equals(other.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, host);
    }

    @Override
    public String toString() {
        return name + "$" + host;
    }


}
