package foundry.veil.quasar.emitters.modules.particle.update.velocity;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateModule;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class VelocityOverLifetimeModule implements UpdateModule {
    Function<Vec3, Vec3> velocityFunction;

    public VelocityOverLifetimeModule(Function<Vec3, Vec3> velocityFunction) {
        this.velocityFunction = velocityFunction;
    }
    @Override
    public void run(QuasarParticle particle) {
        particle.setDeltaMovement(velocityFunction.apply(particle.getDeltaMovement()));
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return null;
    }

    @Override
    public void renderImGuiSettings() {

    }
}
