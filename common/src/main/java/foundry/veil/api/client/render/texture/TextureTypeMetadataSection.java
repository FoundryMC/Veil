package foundry.veil.api.client.render.texture;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public record TextureTypeMetadataSection(TextureType type) {

    public static final Serializer SERIALIZER = new Serializer();

    public enum TextureType {
        TEXTURE_2D,
        TEXTURE_2D_ARRAY,
        TEXTURE_CUBE_MAP;

        private static final TextureType[] VALUES = TextureType.values();
    }

    public static class Serializer implements MetadataSectionSerializer<TextureTypeMetadataSection> {

        @Override
        public TextureTypeMetadataSection fromJson(@NotNull JsonObject json) {
            String type = GsonHelper.getAsString(json, "type", "2d");
            for (TextureType value : TextureType.VALUES) {
                if (value.name().substring(8).equalsIgnoreCase(type)) {
                    return new TextureTypeMetadataSection(value);
                }
            }
            throw new JsonSyntaxException("Unknown texture type: " + type + ". Expected one of " + Arrays.stream(TextureType.VALUES)
                    .map(textureType -> textureType.name().toLowerCase(Locale.ROOT))
                    .collect(Collectors.joining(", ")));
        }

        @Override
        public @NotNull String getMetadataSectionName() {
            return "veil:texture_type";
        }
    }
}
