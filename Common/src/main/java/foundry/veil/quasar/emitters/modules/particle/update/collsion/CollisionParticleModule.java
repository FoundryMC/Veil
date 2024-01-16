package foundry.veil.quasar.emitters.modules.particle.update.collsion;

import com.mojang.serialization.Codec;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ParticleModule;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CollisionParticleModule implements UpdateParticleModule {
    Consumer<QuasarParticle> collisionFunction;

    public CollisionParticleModule(Consumer<QuasarParticle> collisionFunction) {
        this.collisionFunction = collisionFunction;
    }
    @Override
    public void run(QuasarParticle particle) {
        collisionFunction.accept(particle);
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return null;
    }
}
