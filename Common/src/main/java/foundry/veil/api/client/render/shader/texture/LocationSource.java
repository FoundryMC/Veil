package foundry.veil.api.client.render.shader.texture;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

/**
 * Source of a shader texture using a registered texture.
 *
 * @param location The location of the texture
 * @author Ocelot
 */
public record LocationSource(ResourceLocation location) implements ShaderTextureSource {

    public static final Codec<LocationSource> CODEC = ResourceLocation.CODEC.fieldOf("location").xmap(LocationSource::new, LocationSource::location).codec();

    @Override
    public int getId(Context context) {
        return context.getTexture(this.location).getId();
    }

    @Override
    public Type getType() {
        return Type.LOCATION;
    }
}
