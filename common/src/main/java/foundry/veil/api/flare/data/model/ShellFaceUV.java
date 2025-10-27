package foundry.veil.api.flare.data.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.util.CodecUtil;
import org.joml.Vector4f;
import org.joml.Vector4fc;

/**
 * @since 2.5.0
 */
public record ShellFaceUV(Vector4fc uvs, int rotation) {

    public static final Codec<ShellFaceUV> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtil.VECTOR4FC_CODEC.fieldOf("uv").forGetter(ShellFaceUV::uvs),
            Codec.INT.optionalFieldOf("rotation", 0).forGetter(ShellFaceUV::rotation)
    ).apply(instance, ShellFaceUV::new));

    public ShellFaceUV(float u0, float v0, float u1, float v1, int rotation) {
        this(new Vector4f(u0, v0, u1, v1), rotation);
    }

    public float getU(int index) {
        int i = this.getShiftedIndex(index);
        return this.uvs.get(i != 0 && i != 1 ? 2 : 0);
    }

    public float getV(int index) {
        int i = this.getShiftedIndex(index);
        return this.uvs.get(i != 0 && i != 3 ? 3 : 1);
    }

    private int getShiftedIndex(int index) {
        return (index + this.rotation / 90) % 4;
    }
}
