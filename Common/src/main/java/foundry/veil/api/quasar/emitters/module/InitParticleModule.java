package foundry.veil.api.quasar.emitters.module;

import foundry.veil.api.quasar.particle.QuasarParticle;

public interface InitParticleModule extends ParticleModule {

    void init(QuasarParticle particle);
}
