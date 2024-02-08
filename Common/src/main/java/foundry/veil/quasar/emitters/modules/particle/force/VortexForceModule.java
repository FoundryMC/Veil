package foundry.veil.quasar.emitters.modules.particle.force;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.data.module.force.VortexForceData;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class VortexForceModule extends SimplePositionedForce {

    private final Vector3d vortexAxis;
    private double rangeSq;
    private float strength;

    private final Vector3d dot;

    public VortexForceModule(VortexForceData data) {
        this(data.vortexAxis(), data.vortexCenter(), data.localPosition(), data.range(), data.strength());
    }

    public VortexForceModule(Vector3dc vortexAxis,
                             Vector3dc vortexCenter,
                             boolean localPosition,
                             double range,
                             float strength) {
        super(vortexCenter, localPosition);
        this.vortexAxis = vortexAxis.normalize(new Vector3d());
        this.rangeSq = range * range;
        this.strength = strength;
        this.dot = new Vector3d();
    }

    @Override
    public void applyForce(QuasarParticle particle) {
        Vector3d diff = this.getDeltaPosition(particle);
        double distanceSq = diff.lengthSquared();
        if (distanceSq >= this.rangeSq) {
            return;
        }

        // apply force to particle to move around the vortex center on the vortex axis, but do not modify outwards/inwards velocity
        Vector3d particleToCenterOnAxis = diff.sub(this.vortexAxis.mul(diff.dot(this.vortexAxis), this.dot));
        particleToCenterOnAxis.normalize();
        particleToCenterOnAxis.cross(this.vortexAxis).mul(this.strength);
        particle.getVelocity().add(particleToCenterOnAxis);
    }

    public Vector3dc getVortexAxis() {
        return this.vortexAxis;
    }

    public void setVortexAxis(double x, double y, double z) {
        this.vortexAxis.set(x, y, z).normalize();
    }

    public void setVortexAxis(Vector3dc axis) {
        this.setVortexAxis(axis.x(), axis.y(), axis.z());
    }

    public void setRange(double range) {
        this.rangeSq = range * range;
    }

    @Override
    public void setStrength(float strength) {
        this.strength = strength;
    }
}
