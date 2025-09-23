package foundry.veil.api.flare.data.model;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.lang.reflect.Type;
import java.util.Map;

public record ShellElementFace(ShellFaceUV uv, MutableObject<ShellElement> parent) {

    public static final Codec<ShellElementFace> CODEC = ShellFaceUV.CODEC.xmap(uv -> new ShellElementFace(uv, new MutableObject<>()), ShellElementFace::uv);

    public static final Codec<Map<Direction, ShellElementFace>> FULL_CODEC = Codec.simpleMap(Direction.CODEC, CODEC, StringRepresentable.keys(Direction.values())).codec();

    public static class Deserializer implements JsonDeserializer<ShellElementFace> {

        public ShellElementFace deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonobject = json.getAsJsonObject();
            ShellFaceUV uv = context.deserialize(jsonobject, ShellFaceUV.class);
            return new ShellElementFace(uv, new MutableObject<>());
        }
    }

}
