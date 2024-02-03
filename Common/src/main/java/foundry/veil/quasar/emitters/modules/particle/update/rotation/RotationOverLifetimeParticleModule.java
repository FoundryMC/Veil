//package foundry.veil.quasar.emitters.modules.particle.update.rotation;
//
//import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
//import foundry.veil.quasar.data.module.ModuleType;
//import foundry.veil.quasar.emitters.modules.particle.UpdateParticleModule;
//import net.minecraft.world.phys.Vec3;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.function.Function;
//
//public class RotationOverLifetimeParticleModule implements UpdateParticleModule {
//
//    private final Function<Integer, Vec3> rotationFunction;
//
//    public RotationOverLifetimeParticleModule(Function<Integer, Vec3> rotationFunction) {
//        this.rotationFunction = rotationFunction;
//    }
//
//    @Override
//    public void update(QuasarVanillaParticle particle) {
//        particle.addRotation(this.rotationFunction.apply(particle.getAge()));
//    }
//
//    @Override
//    public @Nullable ModuleType<?> getType() {
//        return null;
//    }
//
//}
