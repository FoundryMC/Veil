package foundry.veil.material.types;

import foundry.veil.material.IMaterialField;
import net.minecraft.resources.ResourceLocation;

public class MaterialTextureField implements IMaterialField<ResourceLocation> {
    private ResourceLocation texture;

    public MaterialTextureField(ResourceLocation texture) {
        this.texture = texture;
    }

    public MaterialTextureField() {
        this.texture = new ResourceLocation("veil", "textures/white.png");
    }
    @Override
    public ResourceLocation getValue() {
        return texture;
    }

    @Override
    public void setValue(ResourceLocation value) {
        this.texture = value;
    }

    @Override
    public MaterialFieldType getType() {
        return MaterialFieldType.TEXTURE;
    }
}
