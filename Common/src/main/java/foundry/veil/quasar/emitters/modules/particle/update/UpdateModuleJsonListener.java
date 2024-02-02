package foundry.veil.quasar.emitters.modules.particle.update;

import foundry.veil.Veil;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.veil.quasar.data.module.ParticleModuleData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class UpdateModuleJsonListener extends SimpleJsonResourceReloadListener {

    public UpdateModuleJsonListener() {
        super(Veil.GSON, "quasar/modules/update");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager rm, ProfilerFiller profiler) {
        UpdateModuleRegistry.clearRegisteredModules();
        for(Map.Entry<ResourceLocation, JsonElement> entry : elements.entrySet()){
            ResourceLocation id = entry.getKey();
            DataResult<ParticleModuleData> moduleDataResult = ParticleModuleData.UPDATE_DIRECT_CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if(moduleDataResult.error().isPresent()){
                Veil.LOGGER.error("Could not read %s. %s".formatted(id, moduleDataResult.error().get().message()));
                continue;
            }
            ParticleModuleData module = moduleDataResult.getOrThrow(false, Veil.LOGGER::error);
            UpdateModuleRegistry.register(id, (UpdateParticleModule) module);
        }
    }
}
