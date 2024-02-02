package foundry.veil.quasar.emitters.modules.particle.update;

import foundry.veil.Veil;
import foundry.veil.quasar.data.module.ModuleType;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import foundry.veil.quasar.data.module.ParticleModuleData;
import net.minecraft.resources.ResourceLocation;

@Deprecated
public class UpdateModuleRegistry {

    private static final BiMap<ResourceLocation, ParticleModuleData> MODULES_BY_ID = HashBiMap.create();

    public static void register(ResourceLocation id, ParticleModuleData module) {
        MODULES_BY_ID.put(id, module);
    }

    public static ParticleModuleData getModule(ResourceLocation id) {
        if(!MODULES_BY_ID.containsKey(id)) {
            Veil.LOGGER.error("Update module %s does not exist!".formatted(id));
            return null;
        } else return MODULES_BY_ID.get(id);
    }

    public static ResourceLocation getModuleId(ParticleModuleData module) {
        return MODULES_BY_ID.inverse().get(module);
    }

    public static void clearRegisteredModules() {
        MODULES_BY_ID.clear();
    }
}
