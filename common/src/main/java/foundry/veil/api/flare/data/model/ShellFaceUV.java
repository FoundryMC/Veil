package foundry.veil.api.flare.data.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.util.CodecUtil;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import javax.annotation.Nullable;

public class ShellFaceUV {

    public static final Codec<ShellFaceUV> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtil.VECTOR4FC_CODEC.fieldOf("uv").forGetter(ShellFaceUV::getUVs),
            Codec.INT.optionalFieldOf("rotation", 0).forGetter(ShellFaceUV::getRotation)
    ).apply(instance, ShellFaceUV::new));

    public float[] uvs;
    public final int rotation;

    public ShellFaceUV(@Nullable float[] uvs, int rotation) {
        this.uvs = uvs;
        this.rotation = rotation;
    }

    private ShellFaceUV(Vector4fc uvs, Integer rotation) {
        this(vectorToArray(uvs), rotation);
    }

    public float getU(int index) {
        if (this.uvs == null) {
            throw new NullPointerException("uvs");
        } else {
            int i = this.getShiftedIndex(index);
            return this.uvs[i != 0 && i != 1 ? 2 : 0];
        }
    }

    public float getV(int index) {
        if (this.uvs == null) {
            throw new NullPointerException("uvs");
        } else {
            int i = this.getShiftedIndex(index);
            return this.uvs[i != 0 && i != 3 ? 3 : 1];
        }
    }

    private int getShiftedIndex(int index) {
        return (index + this.rotation / 90) % 4;
    }

    public int getReverseIndex(int index) {
        return (index + 4 - this.rotation / 90) % 4;
    }

    public void setMissingUv(float[] uvs) {
        if (this.uvs == null) {
            this.uvs = uvs;
        }

    }

    private Vector4f getUVs() {
        return new Vector4f(uvs);
    }

    public int getRotation() {
        return rotation;
    }

    private static float[] vectorToArray(Vector4fc uvs) {
        return new float[]{uvs.x(), uvs.y(), uvs.z(), uvs.w()};
    }
}
