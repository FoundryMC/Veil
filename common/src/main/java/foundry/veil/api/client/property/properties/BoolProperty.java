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

public class BoolProperty extends Property<Boolean> {
    public BoolProperty(boolean value) {
        super(PropertyRegistry.BOOL.get(), value);
    }

    @Override
    public void applyValue(String name, ShaderInstance shader) {
        Uniform uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(this.overrideValue ? 1 : 0);
        }
    }

    @Override
    public void modify(Boolean value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
        this.overrideValue = value;
        if (mode == PropertyModifier.PropertyModifierMode.MOLANG) {
            optionalMolang.ifPresent(molang -> {
                try {
                    this.overrideValue = this.getEnvironment().get().resolve(molang.getFirst()) >= 0;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    protected void setQueries(MolangRuntime.Builder builder) {
        super.setQueries(builder);
        builder.setQuery("v", () -> this.overrideValue ? 1.0f : -1.0f);
    }

    @Override
    protected Boolean cloneValue(Boolean value) {
        return value;
    }
}
