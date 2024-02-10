package foundry.veil.quasar.emitters.modules.particle;

import foundry.veil.quasar.client.particle.QuasarParticle;

/**
 * A module instance called once per tick.
 */
public interface UpdateParticleModule extends ParticleModule {

    /**
     * Updates this module with the
     * @param particle     The particle to update for
     */
    void update(QuasarParticle particle);
}
