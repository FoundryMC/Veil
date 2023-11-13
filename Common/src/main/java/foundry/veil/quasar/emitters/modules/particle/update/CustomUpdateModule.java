package foundry.veil.quasar.emitters.modules.particle.update;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CustomUpdateModule implements UpdateModule {
    Consumer<QuasarParticle> updateFunction;

    public CustomUpdateModule(Consumer<QuasarParticle> updateFunction) {
        this.updateFunction = updateFunction;
    }
    @Override
    public void run(QuasarParticle particle) {
        updateFunction.accept(particle);
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
