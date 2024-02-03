package foundry.veil.quasar.emitters.modules.particle.force;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.data.module.force.VectorFieldForceData;
import foundry.veil.quasar.emitters.modules.particle.ForceParticleModule;
import org.joml.Vector3d;

public class VectorFieldForceModule implements ForceParticleModule {

    private final VectorFieldForceData data;
    private final Vector3d temp;

    public VectorFieldForceModule(VectorFieldForceData data) {
        this.data = data;
        this.temp = new Vector3d();
    }

    @Override
    public void applyForce(QuasarParticle particle) {
        Vector3d vector = this.data.vectorField().getVector(particle.getPosition(), this.temp);
        particle.getVelocity().add(vector.mul(this.data.strength()));
    }
}
