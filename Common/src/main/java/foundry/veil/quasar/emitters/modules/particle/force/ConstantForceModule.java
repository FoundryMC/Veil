package foundry.veil.quasar.emitters.modules.particle.force;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.particle.ForceParticleModule;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class ConstantForceModule implements ForceParticleModule {

    private final Vector3dc acceleration;
    private final Vector3dc scale;

    public ConstantForceModule(Vector3dc acceleration, Vector3dc scale) {
        this.acceleration = acceleration;
        this.scale = scale;
    }

    public ConstantForceModule(Vector3dc acceleration) {
        this(acceleration, new Vector3d(1.0));
    }

    @Override
    public void applyForce(QuasarParticle particle) {
        particle.getVelocity().mul(this.scale).add(this.acceleration);
    }
}
