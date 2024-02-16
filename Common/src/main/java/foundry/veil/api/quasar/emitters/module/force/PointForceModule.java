package foundry.veil.api.quasar.emitters.module.force;

import foundry.veil.api.quasar.particle.QuasarParticle;
import foundry.veil.api.quasar.data.module.force.PointForceData;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * A point force is used to apply a force in the direction away from a point.
 */
public class PointForceModule extends SimplePositionedForce {

    private double rangeSq;
    private float strength;

    public PointForceModule(PointForceData data) {
        this(data.point(), data.localPoint(), data.range(), data.strength());
    }

    public PointForceModule(Vector3dc point,
                            boolean localPoint,
                            float range,
                            float strength) {
        super(point, localPoint);
        this.rangeSq = range * range;
        this.strength = strength;
    }

    @Override
    public void applyForce(QuasarParticle particle) {
        Vector3d diff = this.getDeltaPosition(particle);
        double distanceSq = diff.lengthSquared();
        if (distanceSq >= this.rangeSq) {
            return;
        }

        // apply force to particle to move away from the point
        particle.getVelocity().add(diff.normalize(this.strength));
    }

    public void setRange(double range) {
        this.rangeSq = range * range;
    }

    @Override
    public void setStrength(float strength) {
        this.strength = strength;
    }
}
