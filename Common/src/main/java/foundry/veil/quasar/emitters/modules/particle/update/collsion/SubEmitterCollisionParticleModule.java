package foundry.veil.quasar.emitters.modules.particle.update.collsion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.data.ParticleEmitterData;
import foundry.veil.quasar.emitters.ParticleContext;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleEmitterRegistry;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.forces.PointForce;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SubEmitterCollisionParticleModule extends CollisionParticleModule {
    public static final Codec<SubEmitterCollisionParticleModule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("subemitter").forGetter(SubEmitterCollisionParticleModule::getSubEmitter)
    ).apply(instance, SubEmitterCollisionParticleModule::new));
    private ResourceLocation subEmitter;

    public ResourceLocation getSubEmitter() {
        return subEmitter;
    }

    public SubEmitterCollisionParticleModule(ResourceLocation subEmitter) {
        super(particle -> {
            ParticleContext context = particle.getContext();
            ParticleEmitterData emitter = ParticleEmitterRegistry.getEmitter(subEmitter);
            if (emitter == null) return;
            ParticleEmitter instance = new ParticleEmitter(context.getLevel(), emitter);
            instance.setPosition(context.getPosition());
            // TODO: Make this an option inside the force modules
            instance.getParticleData().getForces().forEach(force -> {
                if (force instanceof PointForce pf) {
                    pf.setPoint(context.getPosition());
                }
            });
            ParticleSystemManager.getInstance().addParticleSystem(instance);
        });
        this.subEmitter = subEmitter;
    }

    @Override
    public @NotNull ModuleType<?> getType() {
        return ModuleType.SUB_EMITTER_COLLISION;
    }

}
