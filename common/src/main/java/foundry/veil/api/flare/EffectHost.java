package foundry.veil.api.flare;

/**
 * @since 2.5.0
 */
public interface EffectHost extends AutoCloseable {

    float getValue(String name);

    String getName();

    void update(float partialTick);

    @Override
    default void close() {
        FlareEffectManager.getInstance().getControllerManager().removeHost(this.getName());
    }
}
