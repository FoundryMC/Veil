package foundry.veil.api.quasar.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record SpriteData(ResourceLocation sprite, int frameCount, float frameTime, int frameWidth, int frameHeight) {

    public static final Codec<SpriteData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("sprite").forGetter(SpriteData::sprite),
            Codec.INT.optionalFieldOf("frame_count", 1).forGetter(SpriteData::frameCount),
            Codec.FLOAT.optionalFieldOf("frame_time", 1.0F).forGetter(SpriteData::frameTime),
            Codec.INT.optionalFieldOf("frame_width", 1).forGetter(SpriteData::frameWidth),
            Codec.INT.optionalFieldOf("frame_height", 1).forGetter(SpriteData::frameHeight)
    ).apply(instance, SpriteData::new));
}
