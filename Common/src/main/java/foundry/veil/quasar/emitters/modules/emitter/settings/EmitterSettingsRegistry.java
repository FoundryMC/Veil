package foundry.veil.quasar.emitters.modules.emitter.settings;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;

public class EmitterSettingsRegistry {
    private static final BiMap<ResourceLocation, ParticleSettings> PARTICLE_SETTINGS_BY_ID = HashBiMap.create();
    private static final BiMap<ResourceLocation, EmitterShapeSettings> SHAPE_SETTINGS_BY_ID = HashBiMap.create();
    private static final BiMap<ResourceLocation, EmitterSettingsModuleData> SETTINGS_BY_ID = HashBiMap.create();

    public static void register(ResourceLocation id, ParticleSettings settings) {
        PARTICLE_SETTINGS_BY_ID.put(id, settings);
    }

    public static void register(ResourceLocation id, EmitterShapeSettings settings) {
        SHAPE_SETTINGS_BY_ID.put(id, settings);
    }

    public static void register(ResourceLocation id, EmitterSettingsModuleData settings) {
        SETTINGS_BY_ID.put(id, settings);
    }

    public static ParticleSettings getParticleSettings(ResourceLocation id) {
        return PARTICLE_SETTINGS_BY_ID.get(id);
    }

    public static EmitterShapeSettings getShapeSettings(ResourceLocation id) {
        return SHAPE_SETTINGS_BY_ID.get(id);
    }

    public static EmitterSettingsModuleData getSettings(ResourceLocation id) {
        return SETTINGS_BY_ID.get(id);
    }

    public static ResourceLocation getParticleSettingsId(ParticleSettings settings) {
        return PARTICLE_SETTINGS_BY_ID.inverse().get(settings);
    }

    public static ResourceLocation getShapeSettingsId(EmitterShapeSettings settings) {
        return SHAPE_SETTINGS_BY_ID.inverse().get(settings);
    }

    public static ResourceLocation getSettingsId(EmitterSettingsModuleData settings) {
        return SETTINGS_BY_ID.inverse().get(settings);
    }

    public static void clearRegisteredModules() {
        PARTICLE_SETTINGS_BY_ID.clear();
        SHAPE_SETTINGS_BY_ID.clear();
        SETTINGS_BY_ID.clear();
    }

    public static void clearRegisteredParticleSettings() {
        PARTICLE_SETTINGS_BY_ID.clear();
    }

    public static void clearRegisteredShapeSettings() {
        SHAPE_SETTINGS_BY_ID.clear();
    }

    public static void clearRegisteredSettings() {
        SETTINGS_BY_ID.clear();
    }

    public static void bootstrap() {}
}
