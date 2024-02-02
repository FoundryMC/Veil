package foundry.veil.quasar.emitters.modules.particle.update.collsion;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CollisionParticleModule implements UpdateParticleModule {
    Consumer<QuasarVanillaParticle> collisionFunction;

    public CollisionParticleModule(Consumer<QuasarVanillaParticle> collisionFunction) {
        this.collisionFunction = collisionFunction;
    }
    @Override
    public void update(QuasarVanillaParticle particle) {
        collisionFunction.accept(particle);
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return null;
    }
}
