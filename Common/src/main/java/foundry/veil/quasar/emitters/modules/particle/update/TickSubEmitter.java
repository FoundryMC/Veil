package foundry.veil.quasar.emitters.modules.particle.update;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.ParticleContext;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleEmitterRegistry;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import foundry.veil.quasar.emitters.modules.ModuleType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record TickSubEmitter(ResourceLocation subEmitter, int frequency) implements UpdateParticleModule {

    public static final Codec<TickSubEmitter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("subemitter").forGetter(TickSubEmitter::subEmitter),
            Codec.INT.fieldOf("frequency").forGetter(TickSubEmitter::frequency)
    ).apply(instance, TickSubEmitter::new));

    @Override
    public void run(QuasarParticle particle) {
        if (particle.getAge() % this.frequency != 0) {
            return;
        }

        ParticleContext context = particle.getContext();
        ParticleEmitter emitter = ParticleEmitterRegistry.getEmitter(this.subEmitter);
        if (emitter == null) {
            return;
        }

        ParticleEmitter instance = emitter.instance();
        instance.setPosition(context.particle.getPos());
        instance.setLevel(context.particle.getLevel());
        instance.getEmitterSettingsModule().getEmissionShapeSettings().setRandomSource(context.particle.getLevel().random);
        instance.getEmitterSettingsModule().getEmissionShapeSettings().setPosition(context.particle.getPos());
        ParticleSystemManager.getInstance().addDelayedParticleSystem(instance);
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.TICK_SUB_EMITTER;
    }

}
