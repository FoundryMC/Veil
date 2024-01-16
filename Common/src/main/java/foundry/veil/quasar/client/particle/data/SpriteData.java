package foundry.veil.quasar.client.particle.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.Veil;
import net.minecraft.resources.ResourceLocation;

public class SpriteData {
    public static final ResourceLocation BLANK_TEXTURE = Veil.veilPath("textures/special/blank.png");
    public static final Codec<SpriteData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("sprite").orElse(BLANK_TEXTURE).forGetter(SpriteData::getSprite),
                    Codec.INT.fieldOf("frame_count").orElse(1).forGetter(SpriteData::getFrameCount),
                    Codec.FLOAT.fieldOf("frame_time").orElse(1.0f).forGetter(SpriteData::getFrameTime),
                    Codec.INT.fieldOf("frame_width").orElse(1).forGetter(SpriteData::getFrameWidth),
                    Codec.INT.fieldOf("frame_height").orElse(1).forGetter(SpriteData::getFrameHeight)
            ).apply(instance, SpriteData::new)
            );

    public static final SpriteData BLANK = new SpriteData(BLANK_TEXTURE, 1, 1, 1, 1);

    public ResourceLocation sprite;
    public int frameCount;
    public float frameTime;
    public int frameWidth;
    public int frameHeight;
    public SpriteData(ResourceLocation sprite, int frameCount, float frameTime, int frameWidth, int frameHeight) {
        this.sprite = sprite;
        this.frameCount = frameCount;
        this.frameTime = frameTime;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    public ResourceLocation getSprite() {
        return sprite;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public float getFrameTime() {
        return frameTime;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }
}
