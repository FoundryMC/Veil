package foundry.veil.api.client.property.properties;

import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import java.util.List;
import java.util.Optional;

public class Vec2Property extends Property<Vector2f> {
    public Vec2Property(Vector2fc value) {
        super(PropertyRegistry.VEC2.get(), new Vector2f(value));
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
    }

    @Override
    public void modify(Vector2f value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
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
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    @Override
    protected Vector2f cloneValue(Vector2f value) {
        return new Vector2f(value);
    }
}
