package foundry.veil.quasar.emitters.modules.particle.update;

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

public class UpdateModuleJsonListener extends SimpleJsonResourceReloadListener {
    public UpdateModuleJsonListener() {
        super(Quasar.GSON, "modules/update");
    }
    public static void register(RegisterClientReloadListenersEvent event){
        event.registerReloadListener(new UpdateModuleJsonListener());
        Quasar.LOGGER.info("Registered update module listener");
    }
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager rm, ProfilerFiller profiler) {
        UpdateModuleRegistry.clearRegisteredModules();
        for(Map.Entry<ResourceLocation, JsonElement> entry : elements.entrySet()){
            ResourceLocation id = entry.getKey();
            DataResult<Module> moduleDataResult = UpdateModule.DISPATCH_CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if(moduleDataResult.error().isPresent()){
                Quasar.LOGGER.error("Could not read %s. %s".formatted(id, moduleDataResult.error().get().message()));
                continue;
            }
            Module module = moduleDataResult.getOrThrow(false, Quasar.LOGGER::error);
            UpdateModuleRegistry.register(id, (UpdateModule) module);
        }
    }
}
