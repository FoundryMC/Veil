package foundry.veil.api.quasar.emitters.module.force;

import foundry.veil.api.quasar.particle.QuasarParticle;
import foundry.veil.api.quasar.data.module.force.PointAttractorForceData;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class PointAttractorForceModule extends SimplePositionedForce {

    private double rangeSq;
    private float strength;
    private boolean strengthByDistance;
    private boolean invertDistanceModifier;

    public PointAttractorForceModule(PointAttractorForceData data) {
        this(data.position(), data.localPosition(), data.range(), data.strength(), data.strengthByDistance(), data.invertDistanceModifier());
    }

    public PointAttractorForceModule(Vector3dc position,
                                     boolean localPosition,
                                     float range,
                                     float strength,
                                     boolean strengthByDistance,
                                     boolean invertDistanceModifier) {
        super(position, localPosition);
        this.rangeSq = range * range;
        this.strength = strength;
        this.strengthByDistance = strengthByDistance;
        this.invertDistanceModifier = invertDistanceModifier;
    }

    @Override
    public void applyForce(QuasarParticle particle) {
        Vector3d diff = this.getDeltaPosition(particle);
        double distanceSq = diff.lengthSquared();
        if (distanceSq >= this.rangeSq) {
            return;
        }

        double strength;
        if (this.strengthByDistance) {
            double factor = this.invertDistanceModifier ? (distanceSq / this.rangeSq) * 2 : (1 - distanceSq / this.rangeSq);
            strength = this.strength * factor;
        } else {
            strength = this.strength;
        }
//        if (strengthByDistance && !invertDistanceModifier) {
//            strength = strength * (1 - distanceSq / this.rangeSq);
//        } else if (strengthByDistance && invertDistanceModifier) {
//            strength = strength * (distanceSq / this.rangeSq) * 2;
//        }
        particle.getVelocity().add(diff.normalize(strength));
    }

    public void setRange(double range) {
        this.rangeSq = range * range;
    }

    @Override
    public void setStrength(float strength) {
        this.strength = strength;
    }

    public void setStrengthByDistance(boolean strengthByDistance) {
        this.strengthByDistance = strengthByDistance;
    }

    public void setInvertDistanceModifier(boolean invertDistanceModifier) {
        this.invertDistanceModifier = invertDistanceModifier;
    }
}
