package foundry.veil.quasar.data.module.update;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.data.QuasarParticles;
import foundry.veil.quasar.data.ParticleEmitterData;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import foundry.veil.quasar.emitters.modules.particle.UpdateParticleModule;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public record TickSubEmitterModuleData(ResourceLocation subEmitter, int frequency) implements ParticleModuleData {

    public static final Codec<TickSubEmitterModuleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("subemitter").forGetter(TickSubEmitterModuleData::subEmitter),
            Codec.INT.fieldOf("frequency").forGetter(TickSubEmitterModuleData::frequency)
    ).apply(instance, TickSubEmitterModuleData::new));

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule((UpdateParticleModule) (particle -> {
            if (particle.getAge() % this.frequency != 0) {
                return;
            }

            Registry<ParticleEmitterData> registry = QuasarParticles.registryAccess().registryOrThrow(QuasarParticles.EMITTER);
            ParticleEmitterData emitter = registry.get(this.subEmitter);
            if (emitter == null) {
                return;
            }

            ParticleEmitter instance = new ParticleEmitter(particle.getLevel(), emitter);
            instance.setPosition(particle.getPosition());
            ParticleSystemManager.getInstance().addParticleSystem(instance);
        }));
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.TICK_SUB_EMITTER;
    }
}
