package foundry.veil.quasar.emitters.modules.emitter.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.emitters.modules.emitter.BaseEmitterModule;
import net.minecraft.resources.ResourceLocation;

public record EmitterSettingsModule(EmissionShapeSettings emissionShapeSettings,
                                    EmissionParticleSettings emissionParticleSettings) implements BaseEmitterModule {

    public static final Codec<EmitterSettingsModule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("shape").xmap(
                    EmitterSettingsRegistry::getShapeSettings,
                    EmissionShapeSettings::getRegistryId
            ).forGetter(EmitterSettingsModule::emissionShapeSettings),
            ResourceLocation.CODEC.fieldOf("particle").xmap(
                    EmitterSettingsRegistry::getParticleSettings,
                    EmissionParticleSettings::getRegistryId
            ).forGetter(EmitterSettingsModule::emissionParticleSettings)
    ).apply(instance, EmitterSettingsModule::new));

    public ResourceLocation getRegistryId() {
        return EmitterSettingsRegistry.getSettingsId(this);
    }
}
