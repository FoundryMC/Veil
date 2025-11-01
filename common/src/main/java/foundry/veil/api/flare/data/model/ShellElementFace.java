package foundry.veil.api.flare.data.model;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import java.util.Map;

/**
 * @since 2.5.0
 */
public record ShellElementFace(ShellFaceUV uv) {

    public static final Codec<ShellElementFace> CODEC = ShellFaceUV.CODEC.xmap(ShellElementFace::new, ShellElementFace::uv);
    public static final Codec<Map<Direction, ShellElementFace>> FULL_CODEC = Codec.simpleMap(Direction.CODEC, CODEC, StringRepresentable.keys(Direction.values())).codec();

}
