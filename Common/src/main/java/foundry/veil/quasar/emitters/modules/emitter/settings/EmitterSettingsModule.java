package foundry.veil.quasar.emitters.modules.emitter.settings;

import foundry.veil.quasar.emitters.modules.emitter.BaseEmitterModule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public class EmitterSettingsModule implements BaseEmitterModule {
    // TODO: Get the settings from a "registry" by resource location so you can split up files
    public static final Codec<EmitterSettingsModule> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                ResourceLocation.CODEC.fieldOf("shape").xmap(
                        EmitterSettingsRegistry::getShapeSettings,
                        EmissionShapeSettings::getRegistryId
                ).forGetter(EmitterSettingsModule::getEmissionShapeSettings),
                ResourceLocation.CODEC.fieldOf("particle").xmap(
                        EmitterSettingsRegistry::getParticleSettings,
                        EmissionParticleSettings::getRegistryId
                ).forGetter(EmitterSettingsModule::getEmissionParticleSettings)
        ).apply(instance, EmitterSettingsModule::new);
    });
    public ResourceLocation registryName;
    EmissionShapeSettings emissionShapeSettings;
    EmissionParticleSettings emissionParticleSettings;

    public EmitterSettingsModule(EmissionShapeSettings emissionShapeSettings, EmissionParticleSettings emissionParticleSettings) {
        this.emissionShapeSettings = emissionShapeSettings;
        this.emissionParticleSettings = emissionParticleSettings;
    }

    public ResourceLocation getRegistryId() {
        return registryName;
    }

    public EmitterSettingsModule instance() {
        EmitterSettingsModule instance =  new EmitterSettingsModule(emissionShapeSettings.instance(), emissionParticleSettings.instance());
        instance.registryName = registryName;
        return instance;
    }

    public EmissionShapeSettings getEmissionShapeSettings() {
        return emissionShapeSettings;
    }

    public EmissionParticleSettings getEmissionParticleSettings() {
        return emissionParticleSettings;
    }



}
