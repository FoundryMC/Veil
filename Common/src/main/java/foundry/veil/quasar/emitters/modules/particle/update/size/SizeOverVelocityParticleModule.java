package foundry.veil.quasar.emitters.modules.particle.update.size;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class SizeOverVelocityParticleModule implements UpdateParticleModule {

    private final Function<Vec3, Float> sizeFunction;

    public SizeOverVelocityParticleModule(Function<Vec3, Float> sizeFunction) {
        this.sizeFunction = sizeFunction;
    }

    @Override
    public void run(QuasarParticle particle) {
        particle.setScale(this.sizeFunction.apply(particle.getDeltaMovement()));
    }

    @Override
    public @Nullable ModuleType<?> getType() {
        return null;
    }

}
