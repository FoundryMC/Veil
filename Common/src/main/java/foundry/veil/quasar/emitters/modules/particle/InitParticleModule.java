package foundry.veil.quasar.emitters.modules.particle;

import foundry.veil.quasar.client.particle.QuasarParticle;

public interface InitParticleModule extends ParticleModule {

    void init(QuasarParticle particle);
}
