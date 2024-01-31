package foundry.veil.quasar.emitters.modules.emitter.settings;

import foundry.veil.Veil;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class ShapeSettingsJsonListener extends SimpleJsonResourceReloadListener {

    public ShapeSettingsJsonListener() {
        super(Veil.GSON, "quasar/modules/emitter/shape");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        EmitterSettingsRegistry.clearRegisteredShapeSettings();
        for(Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()){
            ResourceLocation id = entry.getKey();
            DataResult<EmissionShapeSettings> dataResult = EmissionShapeSettings.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if(dataResult.error().isPresent()){
                Veil.LOGGER.error("Could not read %s. %s".formatted(id, dataResult.error().get().message()));
                continue;
            }
            EmitterSettingsRegistry.register(id, dataResult.result().orElseThrow());
        }
    }
}
