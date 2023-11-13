package foundry.veil.quasar.client.particle;

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

public class QuasarParticleDataListener extends SimpleJsonResourceReloadListener {
    public QuasarParticleDataListener() {
        super(Quasar.GSON, "modules/particle_data");
    }

    public static void register(RegisterClientReloadListenersEvent event){
        event.registerReloadListener(new QuasarParticleDataListener());
        Quasar.LOGGER.info("Registered particle data listener");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        QuasarParticleDataRegistry.clearRegisteredData();
        for(Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()){
            ResourceLocation id = entry.getKey();
            DataResult<QuasarParticleData> dataResult = QuasarParticleData.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if(dataResult.error().isPresent()){
                Quasar.LOGGER.error("Could not read %s. %s".formatted(id, dataResult.error().get().message()));
                continue;
            }
            QuasarParticleData data = dataResult.getOrThrow(false, Quasar.LOGGER::error);
            data.registryId = id;
            QuasarParticleDataRegistry.register(id, data);
        }
    }
}
