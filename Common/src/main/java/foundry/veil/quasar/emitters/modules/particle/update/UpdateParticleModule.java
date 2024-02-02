package foundry.veil.quasar.emitters.modules.particle.update;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ParticleModule;

public interface UpdateParticleModule extends ParticleModule {

    void update(QuasarVanillaParticle particle);
}
