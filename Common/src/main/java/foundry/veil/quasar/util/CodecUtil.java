package foundry.veil.quasar.util;

import com.mojang.serialization.Codec;
import org.joml.Vector4f;

import java.util.List;

public class CodecUtil {
    public static final Codec<Vector4f> VECTOR4F_CODEC = Codec.FLOAT.listOf().xmap(list -> {
        if(list.size() != 4) {
            throw new IllegalArgumentException("Vector4f must have 4 elements!");
        }
        return new Vector4f(list.get(0), list.get(1), list.get(2), list.get(3));
    }, vector -> List.of(vector.x(), vector.y(), vector.z(), vector.w()));
}
