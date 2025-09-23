package foundry.veil.api.client.property;

import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import gg.moonflower.molangcompiler.api.MolangExpression;
import foundry.veil.api.flare.modifier.PropertyModifier;

import java.util.List;
import java.util.Optional;

public abstract class InvertibleProperty<T> extends Property<T> {
    protected T overrideInverseValue;
    private final T inverseValue;

    public InvertibleProperty(PropertyRegistry.PropertyType<T, ?> type, T value) {
        super(type, value);
        this.inverseValue = calculateInverse(cloneValue(value));
        this.overrideInverseValue = cloneValue(value);
    }

    protected abstract T calculateInverse(T value);

    @Override
    public void applyValue(String name, ShaderProgram shader) {
        super.applyValue(name, shader);
        this.applyInverseValue(name, shader);
    }

    @Override
    public final void modify(T value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
        this.modifyPreInvert(value, mode, optionalMolang);
        overrideInverseValue = calculateInverse(value);
    }

    public abstract void modifyPreInvert(T value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang);

    public void applyInverseValue(String name, ShaderProgram shader) {
        T originalOverrideValue = this.overrideValue;
        this.overrideValue = overrideInverseValue;
        super.applyValue("I" + name, shader);
        this.overrideValue = originalOverrideValue;
    }

    @Override
    public void resetOverrideValue() {
        super.resetOverrideValue();
        overrideInverseValue = inverseValue;
    }
}
