package foundry.veil.quasar.emitters.modules.particle.update.size;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateModule;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class SizeOverVelocityModule implements UpdateModule {
    Function<Vec3, Float> sizeFunction;

    public SizeOverVelocityModule(Function<Vec3, Float> sizeFunction) {
        this.sizeFunction = sizeFunction;
    }

    @Override
    public void run(QuasarParticle particle) {
        particle.setScale(sizeFunction.apply(particle.getDeltaMovement()));
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return null;
    }

}
