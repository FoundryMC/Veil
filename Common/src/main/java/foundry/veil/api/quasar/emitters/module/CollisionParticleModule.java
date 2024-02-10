package foundry.veil.api.quasar.emitters.module;

import foundry.veil.api.quasar.particle.QuasarParticle;

public interface CollisionParticleModule extends ParticleModule {

    void collide(QuasarParticle particle);
}
