package foundry.veil.api.client.property.properties;

import foundry.veil.api.client.property.ImmutableProperty;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.api.flare.data.effect.FlareMaterial;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Optional;

/**
 * <p>A property that applies a random value each time.</p>
 * Added as a default property named '_Seed' in all {@link FlareMaterial} that have {@link FlareMaterial#useRandomSeed()} set to <code>true<code>.
 *
 * @author GuyApooye
 */
public class RandomFloatProperty extends Property<Float> implements ImmutableProperty {
    private final RandomSource randomSource = RandomSource.create(10841L);
    public static final RandomFloatProperty INSTANCE = new RandomFloatProperty();

    private RandomFloatProperty() {
        super(PropertyRegistry.FLOAT.get(), 0.0f);
    }

    @Override
    public void applyValue(ShaderUniformAccess uniform, int location) {
        uniform.setFloat(randomSource.nextFloat());
    }

    @Override
    public void modify(Float value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
    }

    @Override
    protected Float cloneValue(Float value) {
        return value;
    }
}
