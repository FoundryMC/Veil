package foundry.veil.quasar.emitters.modules.particle.init;

import foundry.veil.quasar.data.module.ModuleType;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;

@Deprecated
public class InitModuleRegistry {

    private static final BiMap<String, ModuleType<?>> MODULES = HashBiMap.create();

    public static final Codec<ModuleType<?>> MODULE_MAP_CODEC = Codec.STRING.comapFlatMap(name -> {
        if(!MODULES.containsKey(name)) {
            return DataResult.error(() -> "Init module %s does not exist!".formatted(name));
        }
        return DataResult.success(MODULES.get(name));
    }, MODULES.inverse()::get);

    public static void register(String name, ModuleType<?> type) {
        MODULES.put(name, type);
    }

    private static final BiMap<ResourceLocation, InitParticleModule> MODULES_BY_ID = HashBiMap.create();

    public static void register(ResourceLocation id, InitParticleModule module) {
        MODULES_BY_ID.put(id, module);
    }

    public static InitParticleModule getModule(ResourceLocation id) {
        return MODULES_BY_ID.get(id);
    }

    public static void clearRegisteredModules() {
        MODULES_BY_ID.clear();
    }

    public static ResourceLocation getModuleId(InitParticleModule initModule) {
        return MODULES_BY_ID.inverse().get(initModule);
    }
}
