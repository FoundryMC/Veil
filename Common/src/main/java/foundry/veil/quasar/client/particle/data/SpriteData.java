package foundry.veil.quasar.client.particle.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.Veil;
import net.minecraft.resources.ResourceLocation;

public record SpriteData(ResourceLocation sprite, int frameCount, float frameTime, int frameWidth, int frameHeight) {

    public static final ResourceLocation BLANK_TEXTURE = Veil.veilPath("textures/special/blank.png");
    public static final SpriteData BLANK = new SpriteData(BLANK_TEXTURE, 1, 1, 1, 1);
    public static final Codec<SpriteData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("sprite").orElse(BLANK_TEXTURE).forGetter(SpriteData::sprite),
                    Codec.INT.fieldOf("frame_count").orElse(1).forGetter(SpriteData::frameCount),
                    Codec.FLOAT.fieldOf("frame_time").orElse(1.0f).forGetter(SpriteData::frameTime),
                    Codec.INT.fieldOf("frame_width").orElse(1).forGetter(SpriteData::frameWidth),
                    Codec.INT.fieldOf("frame_height").orElse(1).forGetter(SpriteData::frameHeight)
            ).apply(instance, SpriteData::new)
    );
}
