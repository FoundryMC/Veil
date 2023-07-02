package foundry.veil.render.shader.program;

import com.google.common.collect.Iterables;
import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.veil.render.shader.texture.ShaderTextureSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40C.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL40C.GL_TESS_EVALUATION_SHADER;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;

/**
 * Defines a shader program instance.
 *
 * @param vertex                The vertex shader or <code>null</code> to not include one
 * @param tesselationControl    The tesselation control shader or <code>null</code> to not include one
 * @param tesselationEvaluation The tesselation evluation shader or <code>null</code> to not include one
 * @param geometry              The geometry shader or <code>null</code> to not include one
 * @param fragment              The fragment shader or <code>null</code> to not include one
 * @param compute               The compute shader or <code>null</code> to not include one.
 *                              Compute should be in a shader by itself
 * @param definitions           The definitions to inject when compiling
 * @param definitionDefaults    The default values for definitions
 * @param textures              The textures to bind when using this shader
 * @param shaders               A map of all sources and their OpenGL types for convenience
 * @author Ocelot
 */
public record ProgramDefinition(@Nullable ResourceLocation vertex,
                                @Nullable ResourceLocation tesselationControl,
                                @Nullable ResourceLocation tesselationEvaluation,
                                @Nullable ResourceLocation geometry,
                                @Nullable ResourceLocation fragment,
                                @Nullable ResourceLocation compute,
                                String[] definitions,
                                Map<String, String> definitionDefaults,
                                Map<String, ShaderTextureSource> textures,
                                Map<Integer, ResourceLocation> shaders) {

    /**
     * Deserializer for {@link ProgramDefinition}.
     */
    public static class Deserializer implements JsonDeserializer<ProgramDefinition> {

        private String[] deserializeDefinitions(JsonArray json, Map<String, String> defaults) throws JsonParseException {

            List<String> definitions = new ArrayList<>(json.size());
            for (int i = 0; i < json.size(); i++) {
                JsonElement element = json.get(i);
                if (element.isJsonPrimitive()) {
                    definitions.add(element.getAsJsonPrimitive().getAsString());
                } else if (element.isJsonObject()) {
                    JsonObject definitionJson = element.getAsJsonObject();
                    Set<Map.Entry<String, JsonElement>> entrySet = definitionJson.entrySet();
                    if (entrySet.size() != 1) {
                        throw new JsonSyntaxException("Expected definitions[" + i + "] " + "to have one element, had " + entrySet.size());
                    }

                    Map.Entry<String, JsonElement> entry = Iterables.getOnlyElement(entrySet);
                    definitions.add(entry.getKey());
                    defaults.put(entry.getKey(), GsonHelper.convertToString(entry.getValue(), "definitions[" + i + "]"));
                } else {
                    throw new JsonSyntaxException("Expected definitions[" + i + "]" + " to be a JsonPrimitive or Object, was " + GsonHelper.getType(element));
                }
            }

            return definitions.toArray(String[]::new);
        }

        private Map<String, ShaderTextureSource> deserializeTextures(JsonObject json) throws JsonParseException {
            Map<String, ShaderTextureSource> textures = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                String name = entry.getKey();
                DataResult<ShaderTextureSource> texture = ShaderTextureSource.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
                if (texture.error().isPresent()) {
                    throw new JsonSyntaxException("Failed to deserialize texture: " + name + ". " + texture.error().get().message());
                }

                textures.put(name, texture.result().orElseThrow());
            }
            ShaderTextureSource.CODEC.parse(JsonOps.INSTANCE, json);
            return Collections.unmodifiableMap(textures);
        }

        @Override
        public ProgramDefinition deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject json = element.getAsJsonObject();
            ResourceLocation vertex = context.deserialize(json.get("vertex"), ResourceLocation.class);
            ResourceLocation tesselationControl = context.deserialize(json.get("tesselation_control"), ResourceLocation.class);
            ResourceLocation tesselationEvaluation = context.deserialize(json.get("tesselation_evaluation"), ResourceLocation.class);
            ResourceLocation geometry = context.deserialize(json.get("geometry"), ResourceLocation.class);
            ResourceLocation fragment = context.deserialize(json.get("fragment"), ResourceLocation.class);
            ResourceLocation compute = context.deserialize(json.get("compute"), ResourceLocation.class);

            String[] definitions;
            Map<String, String> definitionDefaults;
            if (json.has("definitions")) {
                Map<String, String> defaults = new HashMap<>();
                definitions = this.deserializeDefinitions(json.getAsJsonArray("definitions"), defaults);
                definitionDefaults = Collections.unmodifiableMap(defaults);
            } else {
                definitions = new String[0];
                definitionDefaults = Collections.emptyMap();
            }

            Map<String, ShaderTextureSource> textures = json.has("textures") ? this.deserializeTextures(json.getAsJsonObject("textures")) : Collections.emptyMap();

            Map<Integer, ResourceLocation> sources = new HashMap<>();
            if (vertex != null) {
                sources.put(GL_VERTEX_SHADER, vertex);
            }
            if (tesselationControl != null) {
                sources.put(GL_TESS_CONTROL_SHADER, tesselationControl);
            }
            if (tesselationEvaluation != null) {
                sources.put(GL_TESS_EVALUATION_SHADER, tesselationEvaluation);
            }
            if (geometry != null) {
                sources.put(GL_GEOMETRY_SHADER, geometry);
            }
            if (fragment != null) {
                sources.put(GL_FRAGMENT_SHADER, fragment);
            }
            if (compute != null) {
                sources.put(GL_COMPUTE_SHADER, compute);
            }

            return new ProgramDefinition(vertex,
                    tesselationControl,
                    tesselationEvaluation,
                    geometry,
                    fragment,
                    compute,
                    definitions,
                    definitionDefaults,
                    textures,
                    Collections.unmodifiableMap(sources));
        }
    }
}
