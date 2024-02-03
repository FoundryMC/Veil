//package foundry.veil.quasar.emitters.modules.particle.update.velocity;
//
//import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
//import foundry.veil.quasar.data.module.ModuleType;
//import foundry.veil.quasar.emitters.modules.particle.UpdateParticleModule;
//import net.minecraft.world.phys.Vec3;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.function.Function;
//
//public class VelocityParticleModule implements UpdateParticleModule {
//
//    private final Function<Vec3, Vec3> velocityFunction;
//
//    public VelocityParticleModule(Function<Vec3, Vec3> velocityFunction) {
//        this.velocityFunction = velocityFunction;
//    }
//
//    @Override
//    public void update(QuasarVanillaParticle particle) {
//        particle.setDeltaMovement(this.velocityFunction.apply(particle.getDeltaMovement()));
//    }
//
//    @Override
//    public @Nullable ModuleType<?> getType() {
//        return null;
//    }
//
//}
