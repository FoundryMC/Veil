package foundry.veil.quasar.emitters.modules.particle.init;

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
        return this.subEmitter;
    }

    @Override
    public void run(QuasarVanillaParticle particle) {
        ParticleContext context = particle.getContext();
        ParticleEmitterData emitter = ParticleEmitterRegistry.getEmitter(this.subEmitter);
        if (emitter == null) return;
        ParticleEmitter instance = new ParticleEmitter(context.getLevel(), emitter);
        instance.setPosition(context.getPosition());
        ParticleSystemManager.getInstance().addParticleSystem(instance);
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.INIT_SUB_EMITTER;
    }

}
