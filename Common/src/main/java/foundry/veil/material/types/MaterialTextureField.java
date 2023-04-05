package foundry.veil.material.types;

import foundry.veil.material.IMaterialField;
import net.minecraft.resources.ResourceLocation;

public class MaterialTextureField implements IMaterialField {
    private ResourceLocation texture;

    public MaterialTextureField(ResourceLocation texture) {
        this.texture = texture;
    }

    public MaterialTextureField() {
        this.texture = new ResourceLocation("veil", "textures/white.png");
    }
    @Override
    public Object getValue() {
        return texture;
    }

    @Override
    public void setValue(Object value) {
        this.texture = (ResourceLocation) value;
    }

    @Override
    public MaterialFieldType getType() {
        return MaterialFieldType.TEXTURE;
    }
}
