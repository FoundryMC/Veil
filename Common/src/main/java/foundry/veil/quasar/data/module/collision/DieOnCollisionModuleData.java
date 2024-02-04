package foundry.veil.quasar.data.module.collision;

import com.mojang.serialization.Codec;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.modules.particle.CollisionParticleModule;

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
