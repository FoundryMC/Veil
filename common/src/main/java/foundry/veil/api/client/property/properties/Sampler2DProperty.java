package foundry.veil.api.client.property.properties;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.serialization.MapCodec;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL33;

import java.util.List;
import java.util.Optional;

import static com.mojang.blaze3d.platform.GlStateManager.glActiveTexture;
import static org.lwjgl.opengl.GL33.glBindSampler;

public class Sampler2DProperty extends Property<AbstractTexture> {

    private final ResourceLocation source;

    public static final MapCodec<Sampler2DProperty> CODEC = ResourceLocation.CODEC.fieldOf("value").xmap(Sampler2DProperty::new, property -> property.source);

    public Sampler2DProperty(ResourceLocation value) {
        super(PropertyRegistry.SAMPLER2D.get(), Minecraft.getInstance().getTextureManager().getTexture(value));
        source = value;
    }

    @Override
    public void applyValue(ShaderUniformAccess uniform, int location) {
        int originalTexture = GlStateManager._getActiveTexture();
        uniform.setInt(overrideValue.getId());
        glActiveTexture(GL33.GL_TEXTURE0 + overrideValue.getId());
        overrideValue.bind();
        glBindSampler(overrideValue.getId(), 0);
        glActiveTexture(originalTexture);
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
