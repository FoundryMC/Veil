package foundry.veil.api.quasar.emitters.module.force;

import foundry.veil.api.quasar.emitters.module.ForceParticleModule;
import foundry.veil.api.quasar.particle.QuasarParticle;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public abstract class SimplePositionedForce implements ForceParticleModule, PositionedForce {

    protected final Vector3d position;
    protected boolean localPosition;
    private final Vector3d tempPos;

    protected SimplePositionedForce(Vector3d position, boolean localPosition) {
        this.position = position;
        this.localPosition = localPosition;
        this.tempPos = new Vector3d();
    }

    protected SimplePositionedForce(Vector3dc position, boolean localPosition) {
        this(new Vector3d(position), localPosition);
    }

    protected Vector3d getDeltaPosition(QuasarParticle particle) {
        if (this.localPosition) {
            return this.position.add(particle.getParent().getPosition(), this.tempPos).sub(particle.getPosition());
        }
        return this.position.sub(particle.getPosition(), this.tempPos);
    }

    @Override
    public Vector3d getPosition() {
        return this.position;
    }

    @Override
    public boolean isLocalPosition() {
        return this.localPosition;
    }

    @Override
    public void setForceOrigin(double x, double y, double z) {
        this.position.set(x, y, z);
        this.localPosition = false;
    }
}
