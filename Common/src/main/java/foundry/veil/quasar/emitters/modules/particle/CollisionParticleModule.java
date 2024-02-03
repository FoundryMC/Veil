package foundry.veil.quasar.emitters.modules.particle;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ParticleModule;

public interface CollisionParticleModule extends ParticleModule {

    void collide(QuasarParticle particle);
}
