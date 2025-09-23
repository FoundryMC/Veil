package foundry.veil.api.client.property.properties;

import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;

import java.util.List;
import java.util.Optional;

public class BoolProperty extends Property<Boolean> {
    public BoolProperty(boolean value) {
        super(PropertyRegistry.BOOL.get(), value);
    }

    @Override
    public void applyValue(ShaderUniformAccess uniform, int location) {
        uniform.setInt(overrideValue ? 1 : 0);
    }

    @Override
    public void modify(Boolean value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
        this.overrideValue = value;
        if (mode == PropertyModifier.PropertyModifierMode.MOLANG) {
            optionalMolang.ifPresent(molang -> {
                try {
                    this.overrideValue = getEnvironment().get().resolve(molang.getFirst()) >= 0;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    protected void setQueries(MolangRuntime.Builder builder) {
        super.setQueries(builder);
        builder.setQuery("v", () -> overrideValue ? 1.0f : -1.0f);
    }

    @Override
    protected Boolean cloneValue(Boolean value) {
        return value;
    }
}
