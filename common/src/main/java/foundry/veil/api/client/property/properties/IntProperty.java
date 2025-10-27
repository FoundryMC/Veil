package foundry.veil.api.client.property.properties;

import com.mojang.blaze3d.shaders.Uniform;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import net.minecraft.client.renderer.ShaderInstance;

import java.util.List;
import java.util.Optional;

public class IntProperty extends Property<Integer> {
    public IntProperty(int value) {
        super(PropertyRegistry.INT.get(), value);
    }

    @Override
    public void applyValue(String name, ShaderInstance shader) {
        Uniform uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(this.overrideValue);
        }
    }

    @Override
    public void modify(Integer value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
        switch (mode) {
            case ADD -> this.overrideValue += value;
            case SUBTRACT -> this.overrideValue -= value;
            case MULTIPLY -> this.overrideValue *= value;
            case REPLACE -> this.overrideValue = value;
            case MOLANG -> {
                this.overrideValue = value;
                optionalMolang.ifPresent(molang -> {
                    try {
                        this.overrideValue = (int) this.getEnvironment().get().resolve(molang.getFirst());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    @Override
    protected void setQueries(MolangRuntime.Builder builder) {
        super.setQueries(builder);
        builder.setQuery("v", () -> (float) this.overrideValue);
    }

    @Override
    protected Integer cloneValue(Integer value) {
        return value;
    }

}
