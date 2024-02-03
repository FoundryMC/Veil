package foundry.veil.quasar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.emitters.modules.emitter.BaseEmitterModule;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record EmitterSettings(Holder<EmitterShapeSettings> emitterShapeSettingsHolder,
                              Holder<ParticleSettings> particleSettingsHolder) implements BaseEmitterModule {

    public static final Codec<EmitterSettings> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EmitterShapeSettings.CODEC.fieldOf("shape").forGetter(EmitterSettings::emitterShapeSettingsHolder),
            ParticleSettings.CODEC.fieldOf("particle").forGetter(EmitterSettings::particleSettingsHolder)
    ).apply(instance, EmitterSettings::new));
    public static final Codec<Holder<EmitterSettings>> CODEC = RegistryFileCodec.create(QuasarParticles.EMITTER_SETTINGS, DIRECT_CODEC);

    public EmitterShapeSettings emitterShapeSettings() {
        return this.emitterShapeSettingsHolder.value();
    }

    public ParticleSettings particleSettings() {
        return this.particleSettingsHolder.value();
    }

    public @Nullable ResourceLocation getRegistryId() {
        return QuasarParticles.registryAccess().registryOrThrow(QuasarParticles.EMITTER_SETTINGS).getKey(this);
    }
}
