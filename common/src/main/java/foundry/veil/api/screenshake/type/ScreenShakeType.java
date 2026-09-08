package foundry.veil.api.screenshake.type;

import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

/**
 * Defines the behavior of a screenshake
 *
 * @author Neddslayer
 * @since 4.5.0
 */
public abstract class ScreenShakeType {

    private final Vector3f positionOffset = new Vector3f();
    private final RandomSource randomSource;
    private final int length;
    protected int ticksRemaining;

    public ScreenShakeType(RandomSource randomSource, int length) {
        this.randomSource = randomSource;
        this.length = length;
        this.ticksRemaining = this.length;
    }

    @ApiStatus.Internal
    public void tick() {
        this.ticksRemaining--;
        if (!this.isRemoved()) {
            float strength = this.getStrength();
            this.positionOffset.set(this.randomOffset(strength), this.randomOffset(strength), this.randomOffset(strength));
        }
    }

    private float randomOffset(float strength) {
        return (this.randomSource.nextFloat() - 0.5f) * strength;
    }

    public boolean isRemoved() {
        return this.ticksRemaining <= 0;
    }

    /**
     * Immediately remove the screenshake.
     */
    public void remove() {
        this.ticksRemaining = Integer.MIN_VALUE;
    }

    /**
     * @return How long the screenshake has been running for.
     */
    public float age() {
        return this.length - this.ticksRemaining;
    }

    /**
     * @return The lifetime of the screenshake.
     */
    public float length() {
        return this.length;
    }

    /**
     * @return For the current tick, where the screenshake has offset the camera.
     */
    public Vector3f getPositionOffset() {
        return this.positionOffset;
    }

    /**
     * The intensity of the screenshake, up to the implementation of the {@code ScreenShakeType}.
     */
    protected abstract float getStrength();
}
