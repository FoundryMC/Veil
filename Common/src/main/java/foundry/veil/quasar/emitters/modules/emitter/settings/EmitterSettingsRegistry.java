package foundry.veil.quasar.emitters.modules.emitter.settings;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;

public class EmitterSettingsRegistry {
    private static final BiMap<ResourceLocation, EmissionParticleSettings> PARTICLE_SETTINGS_BY_ID = HashBiMap.create();
    private static final BiMap<ResourceLocation, EmissionShapeSettings> SHAPE_SETTINGS_BY_ID = HashBiMap.create();
    private static final BiMap<ResourceLocation, EmitterSettingsModule> SETTINGS_BY_ID = HashBiMap.create();

    public static void register(ResourceLocation id, EmissionParticleSettings settings) {
        PARTICLE_SETTINGS_BY_ID.put(id, settings);
    }

    public static void register(ResourceLocation id, EmissionShapeSettings settings) {
        SHAPE_SETTINGS_BY_ID.put(id, settings);
    }

    public static void register(ResourceLocation id, EmitterSettingsModule settings) {
        SETTINGS_BY_ID.put(id, settings);
    }

    public static EmissionParticleSettings getParticleSettings(ResourceLocation id) {
        return PARTICLE_SETTINGS_BY_ID.get(id);
    }

    public static EmissionShapeSettings getShapeSettings(ResourceLocation id) {
        return SHAPE_SETTINGS_BY_ID.get(id);
    }

    public static EmitterSettingsModule getSettings(ResourceLocation id) {
        return SETTINGS_BY_ID.get(id);
    }

    public static ResourceLocation getParticleSettingsId(EmissionParticleSettings settings) {
        return PARTICLE_SETTINGS_BY_ID.inverse().get(settings);
    }

    public static ResourceLocation getShapeSettingsId(EmissionShapeSettings settings) {
        return SHAPE_SETTINGS_BY_ID.inverse().get(settings);
    }

    public static ResourceLocation getSettingsId(EmitterSettingsModule settings) {
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
