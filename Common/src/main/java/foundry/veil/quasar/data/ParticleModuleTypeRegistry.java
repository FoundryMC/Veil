package foundry.veil.quasar.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;

public class ParticleModuleTypeRegistry {

    private static final BiMap<String, ModuleType<?>> INIT_MODULES = HashBiMap.create();
    private static final BiMap<String, ModuleType<?>> UPDATE_MODULES = HashBiMap.create();
    private static final BiMap<String, ModuleType<?>> RENDER_MODULES = HashBiMap.create();

    public static final Codec<ModuleType<?>> INIT_MODULE_CODEC = Codec.STRING.comapFlatMap(name -> {
        ModuleType<?> module = INIT_MODULES.get(name);
        if (module == null) {
            return DataResult.error(() -> "Init module %s does not exist!".formatted(name));
        }
        return DataResult.success(module);
    }, INIT_MODULES.inverse()::get);

    public static final Codec<ModuleType<?>> UPDATE_MODULE_CODEC = Codec.STRING.comapFlatMap(name -> {
        ModuleType<?> module = UPDATE_MODULES.get(name);
        if (module == null) {
            return DataResult.error(() -> "Update module %s does not exist!".formatted(name));
        }
        return DataResult.success(module);
    }, UPDATE_MODULES.inverse()::get);

    public static final Codec<ModuleType<?>> RENDER_MODULE_CODEC = Codec.STRING.comapFlatMap(name -> {
        ModuleType<?> module = RENDER_MODULES.get(name);
        if (module == null) {
            return DataResult.error(() -> "Render module %s does not exist!".formatted(name));
        }
        return DataResult.success(module);
    }, RENDER_MODULES.inverse()::get);

    public static void registerInit(String name, ModuleType<?> type) {
        INIT_MODULES.put(name, type);
    }

    public static void registerUpdate(String name, ModuleType<?> type) {
        UPDATE_MODULES.put(name, type);
    }

    public static void registerRender(String name, ModuleType<?> type) {
        RENDER_MODULES.put(name, type);
    }

    public static String getName(ModuleType<?> type) {
        String initModuleName = INIT_MODULES.inverse().get(type);
        if (initModuleName != null) {
            return initModuleName;
        }
        String updateModuleName = UPDATE_MODULES.inverse().get(type);
        if (updateModuleName != null) {
            return updateModuleName;
        }
        String renderModuleName = RENDER_MODULES.inverse().get(type);
        if (renderModuleName != null) {
            return renderModuleName;
        }

        return type.getClass().getName();
    }
}