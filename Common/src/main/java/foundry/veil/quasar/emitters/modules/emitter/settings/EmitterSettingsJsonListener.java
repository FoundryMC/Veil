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

public class EmitterSettingsJsonListener extends SimpleJsonResourceReloadListener {
    public EmitterSettingsJsonListener() {
        super(Quasar.GSON, "modules/emitter/settings");
    }

    public static void register(RegisterClientReloadListenersEvent event){
        event.registerReloadListener(new EmitterSettingsJsonListener());
        Quasar.LOGGER.info("Registered emitter settings listener");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        EmitterSettingsRegistry.clearRegisteredSettings();
        for(Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()){
            ResourceLocation id = entry.getKey();
            DataResult<EmitterSettingsModule> dataResult = EmitterSettingsModule.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if(dataResult.error().isPresent()){
                Quasar.LOGGER.error("Could not read %s. %s".formatted(id, dataResult.error().get().message()));
                continue;
            }
            EmitterSettingsModule data = dataResult.getOrThrow(false, Quasar.LOGGER::error);
            data.registryName = id;
            EmitterSettingsRegistry.register(id, data);
        }
    }
}
