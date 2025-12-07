package foundry.veil.api.flare.data.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.*;

/**
 * @since 2.5.0
 */
public final class ShellElement {
    public static final Codec<ShellElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtil.VECTOR3FC_CODEC.fieldOf("from").forGetter(ShellElement::from),
            CodecUtil.VECTOR3FC_CODEC.fieldOf("to").forGetter(ShellElement::to),
            ShellElementRotation.CODEC.optionalFieldOf("rotation").forGetter(element -> Optional.ofNullable(element.rotation)),
            ShellElementFace.FULL_CODEC.fieldOf("faces").forGetter(ShellElement::faces)
    ).apply(instance, (from, to, rotation, faces) -> {
        Map<Direction, ShellElementFace> facesMap = faces.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(new EnumMap<>(faces));
        return new ShellElement(from, to, rotation.orElse(null), facesMap);
    }));
    private final Vector3fc from;
    private final Vector3fc to;
    private final @Nullable ShellElementRotation rotation;
    private final Map<Direction, ShellElementFace> faces;
    
    public ShellElement(
            Vector3fc from,
            Vector3fc to,
            @Nullable ShellElementRotation rotation,
            Map<Direction, ShellElementFace> faces
    ) {
        this.from = from;
        this.to = to;
        this.rotation = rotation;
        this.faces = Map.copyOf(faces);
    }
    
    public Vector3fc from() {
        return from;
    }
    
    public Vector3fc to() {
        return to;
    }
    
    public @Nullable ShellElementRotation rotation() {
        return rotation;
    }
    
    public Map<Direction, ShellElementFace> faces() {
        return faces;
    }
    
}
