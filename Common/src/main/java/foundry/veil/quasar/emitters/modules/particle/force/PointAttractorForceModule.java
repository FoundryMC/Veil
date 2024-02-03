package foundry.veil.quasar.emitters.modules.particle.force;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.data.module.force.PointAttractorForceData;
import foundry.veil.quasar.emitters.modules.particle.ForceParticleModule;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class PointAttractorForceModule implements ForceParticleModule {

    private final PointAttractorForceData data;
    private final double rangeSq;
    private final Vector3d temp;

    public PointAttractorForceModule(PointAttractorForceData data) {
        this.data = data;
        this.rangeSq = data.range() * data.range();
        this.temp = new Vector3d();
    }

    @Override
    public void applyForce(QuasarParticle particle) {
        Vector3d diff = this.data.position().sub(particle.getPosition(), this.temp);
        double distanceSq = diff.lengthSquared();
        if (distanceSq >= this.rangeSq) {
            return;
        }

        double strength;
        if (this.data.strengthByDistance()) {
            double factor = this.data.invertDistanceModifier() ? (distanceSq / this.rangeSq) * 2 : (1 - distanceSq / this.rangeSq);
            strength = this.data.strength() * factor;
        } else {
            strength = this.data.strength();
        }
//        if (strengthByDistance && !invertDistanceModifier) {
//            strength = strength * (1 - distanceSq / this.rangeSq);
//        } else if (strengthByDistance && invertDistanceModifier) {
//            strength = strength * (distanceSq / this.rangeSq) * 2;
//        }
        particle.getVelocity().add(diff.normalize(strength));
    }
}
