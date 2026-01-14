package foundry.veil.api.client.property;

import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Optional;

/**
 * A property that applies its inverse value after applying the main one, adding the "I" prefix to it.
 *
 * @param <T>
 * @author GuyApooye
 */
public abstract class InvertibleProperty<T> extends Property<T> {

    protected T overrideInverseValue;
    private final T inverseValue;

    public InvertibleProperty(PropertyRegistry.PropertyType<T, ?> type, T value) {
        super(type, value);
        this.inverseValue = this.invertAndMutate(this.cloneValue(value));
        this.overrideInverseValue = this.cloneValue(inverseValue);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Contract(mutates = "param")
    protected abstract T invertAndMutate(T value);

    @Override
    public final void modify(T value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
        this.modifyPreInvert(value, mode, optionalMolang);
        
        T originalOverrideValue = this.overrideValue;
        this.overrideValue = overrideInverseValue;
        this.modifyPreInvert(originalOverrideValue, PropertyModifier.PropertyModifierMode.REPLACE, Optional.empty());
        this.overrideValue = originalOverrideValue;
        
        this.invertAndMutate(this.overrideInverseValue);
    }

    public abstract void modifyPreInvert(T value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang);

    public void applyInverseValue(String name, ShaderInstance shader) {
        T originalOverrideValue = this.overrideValue;
        this.overrideValue = this.overrideInverseValue;
        this.applyValue("I" + name, shader);
        this.overrideValue = originalOverrideValue;
    }

    @Override
    public void resetOverrideValue() {
        this.modifyPreInvert(this.value, PropertyModifier.PropertyModifierMode.REPLACE, Optional.empty());
        T originalOverrideValue = this.overrideValue;
        this.overrideValue = overrideInverseValue;
        this.modifyPreInvert(this.inverseValue, PropertyModifier.PropertyModifierMode.REPLACE, Optional.empty());
        this.overrideValue = originalOverrideValue;
    }
}
