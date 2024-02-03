package foundry.veil.quasar.emitters.modules.particle.force;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.data.module.force.PointForceData;
import foundry.veil.quasar.emitters.modules.particle.ForceParticleModule;
import org.joml.Vector3d;

/**
 * A point force is used to apply a force in the direction away from a point.
 */
public class PointForceModule implements ForceParticleModule {

    private final PointForceData data;
    private final double rangeSq;
    private final Vector3d temp;

    public PointForceModule(PointForceData data) {
        this.data = data;
        this.rangeSq = data.range() * data.range();
        this.temp = new Vector3d();
    }

    @Override
    public void applyForce(QuasarParticle particle) {
        Vector3d diff = this.data.point().sub(particle.getPosition(), this.temp);
        double distanceSq = diff.lengthSquared();
        if (distanceSq >= this.rangeSq) {
            return;
        }

        // apply force to particle to move away from the point
        particle.getVelocity().add(diff.normalize(this.data.strength()));
    }
}
