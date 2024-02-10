package foundry.veil.quasar.emitters.modules.particle;

import foundry.veil.quasar.client.particle.QuasarParticle;

/**
 * A single module instance on a {@link QuasarParticle} that has a specific function.
 *
 * @author amo
 * @author Ocelot
 * @see InitParticleModule
 * @see UpdateParticleModule
 * @see RenderParticleModule
 * @see ForceParticleModule
 */
public interface ParticleModule {

    /**
     * Called when the module is removed.
     */
    default void onRemove() {
    }
}
