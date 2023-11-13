package foundry.veil.quasar.emitters;

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

public class ParticleEmitterJsonListener extends SimpleJsonResourceReloadListener {
    public ParticleEmitterJsonListener() {
        super(Quasar.GSON, "emitters");
    }

    public static void register(RegisterClientReloadListenersEvent event){
        event.registerReloadListener(new ParticleEmitterJsonListener());
        Quasar.LOGGER.info("Registered particle emitter listener");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        ParticleEmitterRegistry.clearRegisteredEmitters();
        for(Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()){
            ResourceLocation id = entry.getKey();
            DataResult<ParticleEmitter> dataResult = ParticleEmitter.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if(dataResult.error().isPresent()){
                Quasar.LOGGER.error("Could not read %s. %s".formatted(id, dataResult.error().get().message()));
                continue;
            }
            ParticleEmitter data = dataResult.getOrThrow(false, Quasar.LOGGER::error);
            data.registryName = id;
            ParticleEmitterRegistry.register(id, data);
        }
    }
}
