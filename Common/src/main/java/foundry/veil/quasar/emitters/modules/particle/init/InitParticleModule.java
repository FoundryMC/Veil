package foundry.veil.quasar.emitters.modules.particle.init;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ParticleModule;

public interface InitParticleModule extends ParticleModule {

    void init(QuasarVanillaParticle particle);
}
