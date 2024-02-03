package foundry.veil.quasar.emitters.modules.particle.force;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.data.module.force.VortexForceData;
import foundry.veil.quasar.emitters.modules.particle.ForceParticleModule;
import org.joml.Vector3d;

public class VortexForceModule implements ForceParticleModule {

    private final VortexForceData data;
    private final float rangeSq;
    private final Vector3d normalizedVortexAxis;
    private final Vector3d temp;
    private final Vector3d dot;

    public VortexForceModule(VortexForceData data) {
        this.data = data;
        this.rangeSq = data.range() * data.range();
        this.normalizedVortexAxis = data.vortexAxis().normalize(new Vector3d());
        this.temp = new Vector3d();
        this.dot = new Vector3d();
    }

    @Override
    public void applyForce(QuasarParticle particle) {
        Vector3d diff = this.data.vortexCenter().sub(particle.getPosition(), this.temp);
        double distanceSq = diff.lengthSquared();
        if (distanceSq >= this.rangeSq) {
            return;
        }

        // apply force to particle to move around the vortex center on the vortex axis, but do not modify outwards/inwards velocity
        Vector3d particleToCenterOnAxis = diff.sub(this.data.vortexAxis().mul(diff.dot(this.normalizedVortexAxis), this.dot));
        particleToCenterOnAxis.normalize();
        particleToCenterOnAxis.cross(this.normalizedVortexAxis).mul(this.data.strength());
        particle.getVelocity().add(particleToCenterOnAxis);
    }
}
