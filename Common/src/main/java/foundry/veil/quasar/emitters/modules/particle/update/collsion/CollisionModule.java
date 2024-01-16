package foundry.veil.quasar.emitters.modules.particle.update.collsion;

import com.mojang.serialization.Codec;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.Module;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateModule;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CollisionModule implements UpdateModule {
    Consumer<QuasarParticle> collisionFunction;

    public CollisionModule(Consumer<QuasarParticle> collisionFunction) {
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

    @Override
    public Codec<Module> getDispatchCodec() {
        return null;
    }
}
