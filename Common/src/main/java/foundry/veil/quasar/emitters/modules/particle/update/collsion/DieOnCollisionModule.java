package foundry.veil.quasar.emitters.modules.particle.update.collsion;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class DieOnCollisionModule extends CollisionModule {
    public static final Codec<DieOnCollisionModule> CODEC = Codec.unit(new DieOnCollisionModule(QuasarParticle::remove));
    public DieOnCollisionModule(Consumer<QuasarParticle> collisionFunction) {
        super(QuasarParticle::remove);
    }

    @Override
    public @NotNull ModuleType<?> getType() {
        return ModuleType.DIE_ON_COLLISION;
    }
}
