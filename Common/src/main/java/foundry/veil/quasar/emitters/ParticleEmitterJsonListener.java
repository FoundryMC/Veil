package foundry.veil.quasar.emitters;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.veil.Veil;
import foundry.veil.quasar.data.ParticleEmitterData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

// FIXME
public class ParticleEmitterJsonListener extends SimpleJsonResourceReloadListener {

    public ParticleEmitterJsonListener() {
        super(Veil.GSON, "quasar/emitters");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> emitters, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        ParticleEmitterRegistry.clearRegisteredEmitters();
        for (Map.Entry<ResourceLocation, JsonElement> entry : emitters.entrySet()) {
            ResourceLocation id = entry.getKey();
            DataResult<ParticleEmitterData> dataResult = ParticleEmitterData.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if (dataResult.error().isPresent()) {
                Veil.LOGGER.error("Could not read %s. %s".formatted(id, dataResult.error().get().message()));
                continue;
            }
            ParticleEmitterRegistry.register(id, dataResult.result().orElseThrow());
        }
    }
}
