package foundry.veil.quasar.emitters.modules.particle.update.size;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class SizeOverLifetimeParticleModule implements UpdateParticleModule {

    private final Function<Integer, Float> sizeFunction;

    public SizeOverLifetimeParticleModule(Function<Integer, Float> sizeFunction) {
        this.sizeFunction = sizeFunction;
    }

    @Override
    public void run(QuasarParticle particle) {
        particle.setScale(this.sizeFunction.apply(particle.getAge()));
    }

    @Override
    public @Nullable ModuleType<?> getType() {
        return null;
    }

}
