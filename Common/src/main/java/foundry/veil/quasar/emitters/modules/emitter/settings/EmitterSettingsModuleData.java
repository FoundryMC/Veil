package foundry.veil.quasar.emitters.modules.emitter.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.data.DynamicParticleDataRegistry;
import foundry.veil.quasar.emitters.modules.emitter.BaseEmitterModule;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;

public record EmitterSettingsModuleData(Holder<EmitterShapeSettings> emitterShapeSettings,
                                        Holder<ParticleSettings> emissionParticleSettings) implements BaseEmitterModule {

    public static final Codec<EmitterSettingsModuleData> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EmitterShapeSettings.CODEC.fieldOf("shape").forGetter(EmitterSettingsModuleData::emitterShapeSettings),
            ParticleSettings.CODEC.fieldOf("particle").forGetter(EmitterSettingsModuleData::emissionParticleSettings)
    ).apply(instance, EmitterSettingsModuleData::new));
    public static final Codec<Holder<EmitterSettingsModuleData>> CODEC = RegistryFileCodec.create(DynamicParticleDataRegistry.EMITTER_SETTINGS, DIRECT_CODEC);

    public ResourceLocation getRegistryId() {
        return EmitterSettingsRegistry.getSettingsId(this);
    }
}
