package foundry.veil.api.client.property.properties;

import com.mojang.blaze3d.shaders.Uniform;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.List;
import java.util.Optional;

public class Vec4Property extends Property<Vector4f> {
    public Vec4Property(Vector4fc value) {
        super(PropertyRegistry.VEC4.get(), new Vector4f(value));
    }

    @Override
    public void applyValue(String name, ShaderInstance shader) {
        Uniform uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(this.overrideValue);
        }
    }

    @Override
    protected void setQueries(MolangRuntime.Builder builder) {
        super.setQueries(builder);
        builder.setQuery("x", this.overrideValue::x);
        builder.setQuery("y", this.overrideValue::y);
        builder.setQuery("z", this.overrideValue::z);
        builder.setQuery("w", this.overrideValue::w);
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
                    MolangEnvironment environment = this.getEnvironment().get();
                    if (!molang.isEmpty()) {
                        this.overrideValue.x = environment.safeResolve(molang.getFirst());
                    }
                    if (molang.size() > 1) {
                        this.overrideValue.y = environment.safeResolve(molang.get(1));
                    }
                    if (molang.size() > 2) {
                        this.overrideValue.z = environment.safeResolve(molang.get(2));
                    }
                    if (molang.size() > 2) {
                        this.overrideValue.z = environment.safeResolve(molang.get(2));
                    }
                    if (molang.size() > 3) {
                        this.overrideValue.w = environment.safeResolve(molang.get(3));
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
