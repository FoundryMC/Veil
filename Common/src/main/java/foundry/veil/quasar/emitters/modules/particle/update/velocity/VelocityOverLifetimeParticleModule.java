package foundry.veil.quasar.emitters.modules.particle.update.velocity;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class VelocityOverLifetimeParticleModule implements UpdateParticleModule {

    private final Function<Vec3, Vec3> velocityFunction;

    public VelocityOverLifetimeParticleModule(Function<Vec3, Vec3> velocityFunction) {
        this.velocityFunction = velocityFunction;
    }

    @Override
    public void run(QuasarVanillaParticle particle) {
        particle.setDeltaMovement(this.velocityFunction.apply(particle.getDeltaMovement()));
    }

    @Override
    public @Nullable ModuleType<?> getType() {
        return null;
    }

}
