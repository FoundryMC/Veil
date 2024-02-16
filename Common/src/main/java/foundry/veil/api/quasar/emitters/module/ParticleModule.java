package foundry.veil.api.quasar.emitters.module;

import foundry.veil.api.quasar.particle.QuasarParticle;

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
