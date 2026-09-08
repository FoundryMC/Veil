package foundry.veil.api.client.necromancer.animation.keyframe;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

// todo: support cubic interpolation w/ derivatives
public enum Interpolation {
    /**
     * @since 4.5.0
     */
    STEP(
            (a, b, t) -> t < 1F ? a : b,
            (a, b, t, result) -> result.set(t < 1 ? a : b)
    ),
    LINEAR(
            (a, b, t) -> Mth.lerp(t, a, b),
            Quaternionfc::slerp
    ),
    /**
     * @since 4.5.0
     */
    EASE_IN(
            (a, b, t) -> Mth.lerp(t * t, a, b),
            (a, b, t, result) -> result.set(a).slerp(b, t * t)
    ),
    /**
     * @since 4.5.0
     */
    EASE_OUT(
            (a, b, t) -> Mth.lerp(1F - (1F - t) * (1F - t), a, b),
            (a, b, t, result) -> result.set(a).slerp(b, 1F - (1F - t) * (1F - t))
    ),
    /**
     * @since 4.5.0
     */
    EASE_IN_OUT(
            (a, b, t) -> Mth.lerp(easeInOut(t), a, b),
            (a, b, t, result) -> result.set(a).slerp(b, easeInOut(t))
    );

    /**
     * @deprecated Use {@link #STEP} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    public static final Interpolation NEAREST_NEIGHBOR = STEP;

    private static float easeInOut(float t) {
        return t < 0.5F ? 2F * t * t : 1F - (float) Math.pow(-2F * t + 2F, 2) / 2F;
    }

    private final FloatInterpolator fInterpolator;
    private final QuaternionInterpolator qInterpolator;

    Interpolation(FloatInterpolator fInterpolator, QuaternionInterpolator qInterpolator) {
        this.fInterpolator = fInterpolator;
        this.qInterpolator = qInterpolator;
    }

    public float interpolate(float a, float b, float t) {
        return this.fInterpolator.interpolate(a, b, t);
    }

    public Quaternionf interpolate(Quaternionfc a, Quaternionfc b, float t, Quaternionf result) {
        this.qInterpolator.interpolate(a, b, t, result);
        return result;
    }

    public interface FloatInterpolator {
        float interpolate(float a, float b, float t);
    }

    public interface QuaternionInterpolator {
        void interpolate(Quaternionfc a, Quaternionfc b, float t, Quaternionf result);
    }
}
