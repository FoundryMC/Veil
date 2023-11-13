package foundry.veil.quasar.emitters.modules.emitter.settings;

import foundry.veil.quasar.Quasar;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

import java.util.Map;

public class ParticleSettingsJsonListener extends SimpleJsonResourceReloadListener {
    public ParticleSettingsJsonListener() {
        super(Quasar.GSON, "modules/emitter/particle");
    }

    public static void register(RegisterClientReloadListenersEvent event){
        event.registerReloadListener(new ParticleSettingsJsonListener());
        Quasar.LOGGER.info("Registered particle settings listener");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        EmitterSettingsRegistry.clearRegisteredParticleSettings();
        for(Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()){
            ResourceLocation id = entry.getKey();
            DataResult<EmissionParticleSettings> dataResult = EmissionParticleSettings.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if(dataResult.error().isPresent()){
                Quasar.LOGGER.error("Could not read %s. %s".formatted(id, dataResult.error().get().message()));
                continue;
            }
            EmissionParticleSettings data = dataResult.getOrThrow(false, Quasar.LOGGER::error);
            data.registryName = id;
            EmitterSettingsRegistry.register(id, data);
        }
    }
}
