package foundry.veil.quasar.emitters.modules.particle.update;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.data.module.ModuleType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CustomUpdateParticleModule implements UpdateParticleModule {

    private final Consumer<QuasarVanillaParticle> updateFunction;

    public CustomUpdateParticleModule(Consumer<QuasarVanillaParticle> updateFunction) {
        this.updateFunction = updateFunction;
    }

    @Override
    public void update(QuasarVanillaParticle particle) {
        this.updateFunction.accept(particle);
    }

    @Override
    public @Nullable ModuleType<?> getType() {
        return null;
    }

}
