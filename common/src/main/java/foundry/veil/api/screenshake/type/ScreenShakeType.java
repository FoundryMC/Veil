package foundry.veil.api.screenshake.type;

import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

public abstract class ScreenShakeType {
    private final Vector3f positionOffset = new Vector3f();
    private final RandomSource randomSource;
    protected final int length;
    protected int ticksRemaining;

    public ScreenShakeType(RandomSource randomSource, int length) {
        this.randomSource = randomSource;
        this.length = length;
        this.ticksRemaining = this.length;
    }

    public void tick() {
        this.ticksRemaining--;
        if (!this.isRemoved()) {
            float strength = this.getStrength();
            positionOffset.set(randomOffset(strength), randomOffset(strength), randomOffset(strength));
        }
    }

    private float randomOffset(float strength) {
        return (randomSource.nextFloat() - 0.5f) * strength;
    }

    public boolean isRemoved() {
        return this.ticksRemaining <= 0;
    }

    public void remove() {
        this.ticksRemaining = Integer.MIN_VALUE;
    }

    public Vector3f getPositionOffset() {
        return positionOffset;
    }


    protected abstract float getStrength();
}
