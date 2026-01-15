package foundry.veil.api.client.property.properties;

import com.mojang.serialization.MapCodec;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public class Sampler2DProperty extends Property<AbstractTexture> {

    private final ResourceLocation source;

    public static final MapCodec<Sampler2DProperty> CODEC = ResourceLocation.CODEC.fieldOf("value").xmap(Sampler2DProperty::new, property -> property.source);

    public Sampler2DProperty(ResourceLocation value) {
        super(PropertyRegistry.SAMPLER2D.get(), null);
        this.source = value;
    }

    @Override
    public void applyValue(String name, ShaderInstance shader) {
        shader.setSampler(name, Minecraft.getInstance().getTextureManager().getTexture(source).getId());
        // Shader must be re-applied for the sampler to take effect
        shader.apply();
    }

    @Override
    public void modify(AbstractTexture value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
        this.overrideValue = value;
    }

    @Override
    protected AbstractTexture cloneValue(AbstractTexture value) {
        return value;
    }

}
