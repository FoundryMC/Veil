package foundry.veil.api.quasar.emitters.module;

import foundry.veil.api.quasar.particle.QuasarParticle;

public interface ForceParticleModule extends ParticleModule {

    void applyForce(QuasarParticle particle);

    void setStrength(float strength);

}
