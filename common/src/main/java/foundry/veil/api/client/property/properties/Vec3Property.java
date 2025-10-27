package foundry.veil.api.client.property.properties;

import com.mojang.blaze3d.shaders.Uniform;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Optional;

public class Vec3Property extends Property<Vector3f> {
    public Vec3Property(Vector3fc value) {
        super(PropertyRegistry.VEC3.get(), new Vector3f(value));
    }

    @Override
    public void applyValue(String name, ShaderInstance shader) {
        Uniform uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(this.overrideValue.x, this.overrideValue.y, this.overrideValue.z);
        }
    }

    @Override
    protected void setQueries(MolangRuntime.Builder builder) {
        super.setQueries(builder);
        builder.setQuery("x", this.overrideValue::x);
        builder.setQuery("y", this.overrideValue::y);
        builder.setQuery("z", this.overrideValue::z);
    }

    @Override
    public void modify(Vector3f value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
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
                });
            }
        }
    }

    @Override
    protected Vector3f cloneValue(Vector3f value) {
        return new Vector3f(value);
    }
}
