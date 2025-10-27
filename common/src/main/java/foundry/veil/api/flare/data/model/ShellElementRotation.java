package foundry.veil.api.flare.data.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.core.Direction;
import org.joml.Vector3fc;

public record ShellElementRotation(Vector3fc origin, Direction.Axis axis, int angle) {

    public static final Codec<ShellElementRotation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtil.VECTOR3FC_CODEC.fieldOf("origin").forGetter(ShellElementRotation::origin),
            Direction.Axis.CODEC.fieldOf("axis").forGetter(ShellElementRotation::axis),
            Codec.INT.fieldOf("angle").forGetter(ShellElementRotation::angle)
    ).apply(instance, ShellElementRotation::new));
}
