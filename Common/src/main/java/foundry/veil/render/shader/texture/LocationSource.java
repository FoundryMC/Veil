package foundry.veil.render.shader.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * Source of a shader texture using a registered texture.
 *
 * @param location The location of the texture
 * @author Ocelot
 */
public record LocationSource(ResourceLocation location) implements ShaderTextureSource {

    public static final Codec<LocationSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("location").forGetter(LocationSource::location)
    ).apply(instance, LocationSource::new));

    @Override
    public int getId(Context context) {
        return context.getTexture(this.location).getId();
    }

    @Override
    public Type getType() {
        return Type.LOCATION;
    }
}
