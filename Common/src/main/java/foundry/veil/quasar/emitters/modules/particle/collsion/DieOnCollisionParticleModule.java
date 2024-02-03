//package foundry.veil.quasar.emitters.modules.particle.update.collsion;
//
//import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
//import foundry.veil.quasar.data.module.ModuleType;
//import com.mojang.serialization.Codec;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.function.Consumer;
//
//public class DieOnCollisionParticleModule extends CollisionParticleModule {
//    public static final Codec<DieOnCollisionParticleModule> CODEC = Codec.unit(new DieOnCollisionParticleModule(QuasarVanillaParticle::remove));
//    public DieOnCollisionParticleModule(Consumer<QuasarVanillaParticle> collisionFunction) {
//        super(QuasarVanillaParticle::remove);
//    }
//
//    @Override
//    public @NotNull ModuleType<?> getType() {
//        return ModuleType.DIE_ON_COLLISION;
//    }
//}
