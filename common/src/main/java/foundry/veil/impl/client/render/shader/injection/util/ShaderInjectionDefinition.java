package foundry.veil.impl.client.render.shader.injection.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON-deserialized model for shader injection definitions.
 * @author Vowxky
 */
public record ShaderInjectionDefinition(@Nullable ResourceLocation id,
                                        List<ResourceLocation> targets,
                                        List<ResourceLocation> redirects,
                                        int priority,
                                        @Nullable ResourceLocation replace,
                                        boolean debug) {

    public static final int DEFAULT_PRIORITY = 1000;

    public ShaderInjectionDefinition(@Nullable ResourceLocation id,
                                     @Nullable ResourceLocation target,
                                     List<ResourceLocation> redirects,
                                     int priority,
                                     @Nullable ResourceLocation replace,
                                     boolean debug) {
        this(id, target != null ? List.of(target) : List.of(), redirects, priority, replace, debug);
    }

    public ShaderInjectionDefinition {
        if (priority == 0) {
            priority = DEFAULT_PRIORITY;
        }
        targets = List.copyOf(targets);
        redirects = List.copyOf(redirects);
    }

    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(ResourceLocation.class, (JsonDeserializer<ResourceLocation>) (json, type, context) -> ResourceLocation.parse(json.getAsString()))
                .registerTypeAdapter(ResourceLocation.class, (JsonSerializer<ResourceLocation>) (location, type, context) -> context.serialize(location.toString()))
                .registerTypeAdapter(ShaderInjectionDefinition.class, new Deserializer())
                .create();
    }

    public ShaderInjectionDefinition withDefaultId(ResourceLocation defaultId) {
        return new ShaderInjectionDefinition(
                this.id != null ? this.id : defaultId,
                this.targets,
                this.redirects,
                this.priority,
                this.replace,
                this.debug
        );
    }

    public @Nullable ResourceLocation target() {
        return this.targets.isEmpty() ? null : this.targets.getFirst();
    }

    private static class Deserializer implements JsonDeserializer<ShaderInjectionDefinition> {

        @Override
        public ShaderInjectionDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            List<ResourceLocation> targets = parseResourceLocations(obj, "target");
            List<ResourceLocation> redirects = parseResourceLocations(obj, "redirect");
            int priority = obj.has("priority") ? obj.get("priority").getAsInt() : DEFAULT_PRIORITY;
            ResourceLocation replace = obj.has("replace") ? context.deserialize(obj.get("replace"), ResourceLocation.class) : null;
            boolean debug = obj.has("debug") && obj.get("debug").getAsBoolean();

            return new ShaderInjectionDefinition(null, targets, redirects, priority, replace, debug);
        }
    }

    private static List<ResourceLocation> parseResourceLocations(JsonObject obj, String field) {
        if (!obj.has(field) || obj.get(field) instanceof JsonNull) {
            return List.of();
        }
        JsonElement element = obj.get(field);
        if (element instanceof JsonArray array) {
            List<ResourceLocation> result = new ArrayList<>();
            for (JsonElement el : array) {
                result.add(ResourceLocation.parse(el.getAsString()));
            }
            return result;
        }
        return List.of(ResourceLocation.parse(element.getAsString()));
    }
}
