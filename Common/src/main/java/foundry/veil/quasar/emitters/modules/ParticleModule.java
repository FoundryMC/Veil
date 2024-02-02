package foundry.veil.quasar.emitters.modules;

public interface ParticleModule {

    /**
     * Called when the module is removed.
     */
    default void onRemove() {
    }
}
