package foundry.veil.quasar.emitters.modules.particle.update;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.data.ParticleEmitterData;
import foundry.veil.quasar.emitters.ParticleContext;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleEmitterRegistry;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import foundry.veil.quasar.emitters.modules.ModuleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record TickSubEmitter(ResourceLocation subEmitter, int frequency) implements UpdateParticleModule {

    public static final Codec<TickSubEmitter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("subemitter").forGetter(TickSubEmitter::subEmitter),
            Codec.INT.fieldOf("frequency").forGetter(TickSubEmitter::frequency)
    ).apply(instance, TickSubEmitter::new));

    @Override
    public void run(QuasarVanillaParticle particle) {
        if (particle.getAge() % this.frequency != 0) {
            return;
        }

        ParticleContext context = particle.getContext();
        ParticleEmitterData emitter = ParticleEmitterRegistry.getEmitter(this.subEmitter);
        if (emitter == null) {
            return;
        }

        Level level = context.getLevel();
        Vec3 position = context.getPosition();

        ParticleEmitter instance = new ParticleEmitter(level, emitter);
        instance.setPosition(position);
        ParticleSystemManager.getInstance().addParticleSystem(instance);
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.TICK_SUB_EMITTER;
    }

}
