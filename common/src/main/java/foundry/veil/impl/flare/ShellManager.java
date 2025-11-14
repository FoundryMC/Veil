package foundry.veil.impl.flare;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.veil.Veil;
import foundry.veil.api.flare.data.model.FlareShell;
import foundry.veil.api.flare.model.BakedShell;
import foundry.veil.api.flare.model.ShellBakery;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.NativeResource;

import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public class ShellManager extends SimplePreparableReloadListener<Map<ResourceLocation, BakedShell>> implements NativeResource {

    private static final FileToIdConverter CONVERTER = FileToIdConverter.json("flare/shells");

    private Map<ResourceLocation, BakedShell> shells;

    public ShellManager() {
        this.shells = Map.of();
    }

    @Override
    protected @NotNull Map<ResourceLocation, BakedShell> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, BakedShell> data = new HashMap<>();

        Map<ResourceLocation, Resource> resources = CONVERTER.listMatchingResources(resourceManager);
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            ResourceLocation id = CONVERTER.fileToId(location);

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                DataResult<FlareShell> result = FlareShell.CODEC.parse(JsonOps.INSTANCE, element);

                if (result.error().isPresent()) {
                    throw new JsonSyntaxException(result.error().get().message());
                }

                if (data.put(id, result.result().orElseThrow().bake()) != null) {
                    throw new IllegalStateException("Duplicate data file ignored with ID " + id);
                }
            } catch (Exception e) {
                Veil.LOGGER.error("Couldn't parse data file {} from {}", id, location, e);
            }
        }

        return data;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, BakedShell> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        this.free();
        this.shells = Collections.unmodifiableMap(map);
    }

    public BakedShell getBakedShell(ResourceLocation shellLocation) {
        return this.shells.getOrDefault(shellLocation, ShellBakery.MISSING_SHELL);
    }

    @Override
    public void free() {
        for (BakedShell shell : this.shells.values()) {
            shell.free();
        }
        this.shells.clear();
    }
}
