package foundry.veil.api.flare.data.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.core.Direction;
import org.joml.Vector3fc;

/**
 * @param origin The origin of the rotation
 * @param axis   The axis to rotate about
 * @param angle  The angle to apply in degrees
 * @since 2.5.0
 */
public record ShellElementRotation(Vector3fc origin, Direction.Axis axis, float angle) {

    public static final Codec<ShellElementRotation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtil.VECTOR3FC_CODEC.fieldOf("origin").forGetter(ShellElementRotation::origin),
            Direction.Axis.CODEC.fieldOf("axis").forGetter(ShellElementRotation::axis),
            Codec.FLOAT.fieldOf("angle").forGetter(ShellElementRotation::angle)
    ).apply(instance, ShellElementRotation::new));
}
