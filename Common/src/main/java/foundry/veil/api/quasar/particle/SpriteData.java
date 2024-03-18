package foundry.veil.api.quasar.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record SpriteData(ResourceLocation sprite, int frameCount, float frameTime, int frameWidth, int frameHeight,
                         boolean stretchToLifetime) {

    public static final Codec<SpriteData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("sprite").forGetter(SpriteData::sprite),
            Codec.INT.optionalFieldOf("frame_count", 1).forGetter(SpriteData::frameCount),
            Codec.FLOAT.optionalFieldOf("frame_time", 1.0F).forGetter(SpriteData::frameTime),
            Codec.INT.optionalFieldOf("frame_width", 1).forGetter(SpriteData::frameWidth),
            Codec.INT.optionalFieldOf("frame_height", 1).forGetter(SpriteData::frameHeight),
            Codec.BOOL.optionalFieldOf("stretch_to_lifetime", false).forGetter(SpriteData::stretchToLifetime)
    ).apply(instance, SpriteData::new));

    public float u(float renderAge, float agePercent, float u) {
        if (this.frameWidth <= 1) {
            return u;
        }

        int frameIndex = this.stretchToLifetime ? (int) Math.min(agePercent * (this.frameCount + 1), this.frameCount) : (int) (renderAge / this.frameTime);
        int frameRow = frameIndex % this.frameWidth;
        return (float) frameRow / this.frameWidth * (1.0F - u) + (frameRow + 1.0F) / this.frameWidth * u;
    }

    public float v(float renderAge, float agePercent, float v) {
        if (this.frameHeight <= 1) {
            return v;
        }

        int frameIndex = this.stretchToLifetime ? (int) Math.min(agePercent * (this.frameCount + 1), this.frameCount) : (int) (renderAge / this.frameTime);
        int frameColumn = frameIndex / this.frameWidth;
        return (float) frameColumn / this.frameHeight * (1.0F - v) + (frameColumn + 1.0F) / this.frameHeight * v;
    }
}
