package foundry.veil.api.client.property.properties;

import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.List;
import java.util.Optional;

public class Vec4Property extends Property<Vector4f> {
    public Vec4Property(Vector4fc value) {
        super(PropertyRegistry.VEC4.get(), new Vector4f(value));
    }

    @Override
    public void applyValue(ShaderUniformAccess uniform, int location) {
        uniform.setVector(overrideValue);
    }

    @Override
    protected void setQueries(MolangRuntime.Builder builder) {
        super.setQueries(builder);
        builder.setQuery("x", overrideValue::x);
        builder.setQuery("y", overrideValue::y);
        builder.setQuery("z", overrideValue::z);
        builder.setQuery("w", overrideValue::w);
    }

    @Override
    public void modify(Vector4f value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
        switch (mode) {
            case ADD -> this.overrideValue.add(value);
            case SUBTRACT -> this.overrideValue.sub(value);
            case MULTIPLY -> this.overrideValue.mul(value);
            case REPLACE -> this.overrideValue.set(value);
            case MOLANG -> {
                this.overrideValue.set(value);
                optionalMolang.ifPresent(molang -> {
                    try {
                        this.overrideValue.x = getEnvironment().get().resolve(molang.get(0));
                        this.overrideValue.y = getEnvironment().get().resolve(molang.get(1));
                        this.overrideValue.z = getEnvironment().get().resolve(molang.get(2));
                        this.overrideValue.w = getEnvironment().get().resolve(molang.get(3));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    @Override
    protected Vector4f cloneValue(Vector4f value) {
        return new Vector4f(value);
    }
}
