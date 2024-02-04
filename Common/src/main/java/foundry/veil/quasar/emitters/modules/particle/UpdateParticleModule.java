package foundry.veil.quasar.emitters.modules.particle;

import foundry.veil.quasar.client.particle.QuasarParticle;

public interface UpdateParticleModule extends ParticleModule {

    void update(QuasarParticle particle);
}
