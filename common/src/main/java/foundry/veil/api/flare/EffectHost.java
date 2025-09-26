package foundry.veil.api.flare;

public interface EffectHost extends AutoCloseable {
    float getValue(String name);
    String getName();
    void update(float partialTick);

    @Override
    default void close() {
        FlareEffectManager.getInstance().getControllerManager().removeHost(getName());
    }
}
