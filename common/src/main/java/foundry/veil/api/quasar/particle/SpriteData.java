package foundry.veil.api.quasar.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector4f;

public record SpriteData(ResourceLocation sprite,
                         int frameCount,
                         float frameTime,
                         int frameWidth,
                         int frameHeight,
                         boolean stretchToLifetime) {

    public static final Codec<SpriteData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("sprite").forGetter(SpriteData::sprite),
            Codec.INT.optionalFieldOf("frame_count", 1).forGetter(SpriteData::frameCount),
            Codec.FLOAT.optionalFieldOf("frame_time", 1.0F).forGetter(SpriteData::frameTime),
            Codec.INT.optionalFieldOf("frame_width", 1).forGetter(SpriteData::frameWidth),
            Codec.INT.optionalFieldOf("frame_height", 1).forGetter(SpriteData::frameHeight),
            Codec.BOOL.optionalFieldOf("stretch_to_lifetime", false).forGetter(SpriteData::stretchToLifetime)
    ).apply(instance, SpriteData::new));

    /**
     * Calculates the UV x, y, width, and height.
     *
     * @param renderAge  The age of the particle in ticks
     * @param agePercent The percentage age from 0 to 1
     * @param store      The vector to store into
     * @since 1.3.0
     */
    public Vector4f uv(float renderAge, float agePercent, Vector4f store) {
        int frameIndex = this.stretchToLifetime ? (int) Math.min(agePercent * (this.frameCount + 1), this.frameCount) : (int) (renderAge / this.frameTime);
        int frameColumn = frameIndex / this.frameWidth;

        if (this.frameWidth > 1) {
            int frameRow = frameIndex % this.frameWidth;
            store.x = (float) frameRow / this.frameWidth;
            store.z = (frameRow + 1.0F) / this.frameWidth;
        } else {
            store.x = 0;
            store.z = 1;
        }

        if (this.frameHeight > 1) {
            store.y = (float) frameColumn / this.frameHeight;
            store.w = (frameColumn + 1.0F) / this.frameHeight;
        } else {
            store.y = 0;
            store.w = 1;
        }

        return store;
    }

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
