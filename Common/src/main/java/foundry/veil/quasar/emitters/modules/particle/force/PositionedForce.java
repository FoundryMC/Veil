package foundry.veil.quasar.emitters.modules.particle.force;

import org.joml.Vector3d;
import org.joml.Vector3dc;

public interface PositionedForce {

    /**
     * @return The position of this force in emitter-relative space if {@link #isLocalPosition()} is <code>true</code>
     */
    Vector3d getPosition();

    /**
     * @return Whether the position of this force is relative to the emitter
     */
    boolean isLocalPosition();

    /**
     * Directly sets the force origin to a global position.
     *
     * @param x The new origin position X
     * @param y The new origin position Y
     * @param z The new origin position Z
     */
    void setForceOrigin(double x, double y, double z);

    /**
     * Directly sets the force origin to a global position.
     *
     * @param pos The new origin position
     */
    default void setForceOrigin(Vector3dc pos) {
        this.setForceOrigin(pos.x(), pos.y(), pos.z());
    }
}
