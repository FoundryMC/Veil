package foundry.veil.quasar.emitters.modules.particle;

import foundry.veil.quasar.client.particle.QuasarParticle;

public interface ForceParticleModule extends ParticleModule {

    void applyForce(QuasarParticle particle);

}
