package foundry.veil.quasar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.data.QuasarParticleData;
import foundry.veil.quasar.client.particle.data.QuasarParticleDataRegistry;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmitterSettingsModule;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmitterSettingsRegistry;
import net.minecraft.resources.ResourceLocation;

public class ParticleEmitterData {

    public static final Codec<ParticleEmitterData> CODEC = RecordCodecBuilder.create(i ->
            i.group(
                    Codec.INT.fieldOf("max_lifetime").forGetter(ParticleEmitterData::getMaxLifetime),
                    Codec.BOOL.optionalFieldOf("loop", false).forGetter(ParticleEmitterData::isLoop),
                    Codec.INT.fieldOf("rate").forGetter(ParticleEmitterData::getRate),
                    Codec.INT.fieldOf("count").forGetter(ParticleEmitterData::getCount),
                    ResourceLocation.CODEC.fieldOf("emitter_settings").xmap(
                            EmitterSettingsRegistry::getSettings,
                            EmitterSettingsModule::getRegistryId
                    ).forGetter(ParticleEmitterData::getEmitterSettingsModule),
                    ResourceLocation.CODEC.fieldOf("particle_data").xmap(
                            QuasarParticleDataRegistry::getData,
                            QuasarParticleData::getRegistryId
                    ).forGetter(ParticleEmitterData::getParticleData)
            ).apply(i, ParticleEmitterData::new)
    );

    private final int maxLifetime;
    private final boolean loop;
    private final int rate;
    private final int count;
    private final EmitterSettingsModule emitterSettingsModule;
    private final QuasarParticleData data;

    public ParticleEmitterData(int maxLifetime, boolean loop, int rate, int count, EmitterSettingsModule emitterSettingsModule, QuasarParticleData data) {
        this.maxLifetime = maxLifetime;
        this.loop = loop;
        this.rate = rate;
        this.count = count;
        this.emitterSettingsModule = emitterSettingsModule;
        this.data = data;
        // FIXME
        this.data.particleSettings = emitterSettingsModule.emissionParticleSettings();
    }

    /**
     * The rate at which particles are emitted. Count particles per rate ticks.
     */
    public int getRate() {
        return this.rate;
    }

    /**
     * The number of particles emitted per rate ticks
     */
    public int getCount() {
        return this.count;
    }

    /**
     * The maximum number of ticks the emitter will be active for
     */
    public int getMaxLifetime() {
        return this.maxLifetime;
    }

    /**
     * Whether the emitter will loop. If true, the emitter will reset after maxLifetime ticks
     */
    public boolean isLoop() {
        return this.loop;
    }

    // FIXME
    public QuasarParticleData getParticleData() {
        return this.data;
    }

    public EmitterSettingsModule getEmitterSettingsModule() {
        return this.emitterSettingsModule;
    }

}
