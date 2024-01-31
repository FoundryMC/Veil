package foundry.veil.quasar.client.particle.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.veil.Veil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class QuasarParticleDataListener extends SimpleJsonResourceReloadListener {
    public QuasarParticleDataListener() {
        super(Veil.GSON, "quasar/modules/particle_data");
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        QuasarParticleDataRegistry.clearRegisteredData();
        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation id = entry.getKey();
            DataResult<QuasarParticleData> dataResult = QuasarParticleData.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if (dataResult.error().isPresent()) {
                Veil.LOGGER.error("Could not read %s. %s".formatted(id, dataResult.error().get().message()));
                continue;
            }
            QuasarParticleDataRegistry.register(id, dataResult.result().orElseThrow());
        }
    }
}
