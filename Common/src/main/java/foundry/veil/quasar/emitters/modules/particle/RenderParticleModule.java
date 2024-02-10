package foundry.veil.quasar.emitters.modules.particle;

import foundry.veil.quasar.client.particle.QuasarParticle;

/**
 * A module instance called once per frame.
 *
 * @author amo, Ocelot
 */
public interface RenderParticleModule extends ParticleModule {

    /**
     * Called each frame to update render data if {@link #isEnabled()} is <code>true</code>.
     *
     * @param particle     The particle to update for
     * @param partialTicks The percentage from last tick to this tick
     */
    void render(QuasarParticle particle, float partialTicks);

    /**
     * @return Whether this module is enabled and should be considered for the next few frames
     */
    default boolean isEnabled() {
        return true;
    }
}
