package foundry.veil.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads files similarly to {@link SimpleJsonResourceReloadListener},
 * but also decodes them using the provided codec.
 *
 * @param <T> The type of mapped data to return
 * @see Codec
 */
public abstract class CodecReloadListener<T> extends SimplePreparableReloadListener<Map<ResourceLocation, T>> {

    protected final Logger logger;
    protected final Codec<T> codec;
    protected final FileToIdConverter converter;

    /**
     * Creates a new codec reload listener.
     *
     * @param codec     The codec to use when deserializing files
     * @param converter The converter to use for listing files
     */
    public CodecReloadListener(Codec<T> codec, FileToIdConverter converter) {
        this.logger = LogUtils.getLogger();
        this.codec = codec;
        this.converter = converter;
    }

    @Override
    protected Map<ResourceLocation, T> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, T> data = new HashMap<>();

        Map<ResourceLocation, Resource> resources = this.converter.listMatchingResources(resourceManager);
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            ResourceLocation id = this.converter.fileToId(location);

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                DataResult<T> result = this.codec.parse(JsonOps.INSTANCE, element);

                if (result.error().isPresent()) {
                    throw new JsonSyntaxException(result.error().get().message());
                }
                if (data.put(id, result.result().orElseThrow()) != null) {
                    throw new IllegalStateException("Duplicate data file ignored with ID " + id);
                }
            } catch (Exception e) {
                this.logger.error("Couldn't parse data file {} from {}", id, location, e);
            }
        }

        return data;
    }
}
