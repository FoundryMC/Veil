package foundry.veil.quasar.emitters.modules.particle.init;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.ParticleContext;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleEmitterRegistry;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class InitSubEmitter implements InitParticleModule {
    public static final Codec<InitSubEmitter> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("subemitter").forGetter(InitSubEmitter::getSubEmitter)
            ).apply(instance, InitSubEmitter::new));
    ResourceLocation subEmitter;

    public InitSubEmitter(ResourceLocation subEmitter) {
        this.subEmitter = subEmitter;
    }

    public ResourceLocation getSubEmitter() {
        return subEmitter;
    }

    @Override
    public void run(QuasarParticle particle) {
        ParticleContext context = particle.getContext();
        ParticleEmitter emitter = ParticleEmitterRegistry.getEmitter(subEmitter).instance();
        if(emitter == null) return;
        emitter.setPosition(context.particle.getPos());
        emitter.setLevel(context.particle.getLevel());
        emitter.getEmitterSettingsModule().getEmissionShapeSettings().setRandomSource(context.particle.getLevel().random);
        emitter.getEmitterSettingsModule().getEmissionShapeSettings().setPosition(context.particle.getPos());
        ParticleSystemManager.getInstance().addDelayedParticleSystem(emitter);
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.INIT_SUB_EMITTER;
    }

}
