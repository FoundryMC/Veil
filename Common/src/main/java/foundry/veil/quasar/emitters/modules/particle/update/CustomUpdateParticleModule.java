package foundry.veil.quasar.emitters.modules.particle.update;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CustomUpdateParticleModule implements UpdateParticleModule {

    private final Consumer<QuasarParticle> updateFunction;

    public CustomUpdateParticleModule(Consumer<QuasarParticle> updateFunction) {
        this.updateFunction = updateFunction;
    }

    @Override
    public void run(QuasarParticle particle) {
        this.updateFunction.accept(particle);
    }

    @Override
    public @Nullable ModuleType<?> getType() {
        return null;
    }

}
