package foundry.veil.api.quasar.data.module.collision;

import com.mojang.serialization.Codec;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import foundry.veil.api.quasar.particle.QuasarParticle;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.CollisionParticleModule;

public class DieOnCollisionModuleData implements ParticleModuleData {

    public static final Codec<DieOnCollisionModuleData> CODEC = Codec.unit(new DieOnCollisionModuleData());

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule((CollisionParticleModule) QuasarParticle::remove);
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.DIE_ON_COLLISION;
    }
}
