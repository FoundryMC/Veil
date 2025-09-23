package foundry.veil.api.flare;

public interface EffectHost {
    float getValue(String name);
    String getName();
    void update(float partialTick);
}
