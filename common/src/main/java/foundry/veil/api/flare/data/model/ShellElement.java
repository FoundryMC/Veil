package foundry.veil.api.flare.data.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;

public record ShellElement(Vector3f from, Vector3f to, Map<Direction, ShellElementFace> faces,
                           @Nullable ShellElementRotation rotation) {
    public static final Codec<ShellElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtil.VECTOR3FC_CODEC.fieldOf("from").forGetter(ShellElement::from),
            CodecUtil.VECTOR3FC_CODEC.fieldOf("to").forGetter(ShellElement::to),
            ShellElementRotation.CODEC.optionalFieldOf("rotation").forGetter(ShellElement::getRotation),
            ShellElementFace.FULL_CODEC.fieldOf("faces").forGetter(ShellElement::faces)
    ).apply(instance, ShellElement::create));

    private static ShellElement create(Vector3fc from, Vector3fc to, Optional<ShellElementRotation> rotation, Map<Direction, ShellElementFace> faces) {
        return new ShellElement(new Vector3f(from), new Vector3f(to), faces, rotation.orElse(null));
    }

    private Optional<ShellElementRotation> getRotation() {
        return Optional.ofNullable(rotation);
    }
}
