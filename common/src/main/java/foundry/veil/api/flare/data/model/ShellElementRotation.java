package foundry.veil.api.flare.data.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record ShellElementRotation(Vector3f origin, Direction.Axis axis, int angle) {
    public static final Codec<ShellElementRotation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("angle").forGetter(ShellElementRotation::angle),
        Direction.Axis.CODEC.fieldOf("axis").forGetter(ShellElementRotation::axis),
        CodecUtil.VECTOR3FC_CODEC.fieldOf("origin").forGetter(ShellElementRotation::origin)
    ).apply(instance, ShellElementRotation::create));

    private static ShellElementRotation create(Integer angle, Direction.Axis axis, Vector3fc origin) {
        return new ShellElementRotation(new Vector3f(origin), axis, angle);
    }
}
