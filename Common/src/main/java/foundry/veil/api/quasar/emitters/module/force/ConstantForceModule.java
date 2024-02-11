package foundry.veil.api.quasar.emitters.module.force;

import foundry.veil.api.quasar.emitters.module.ForceParticleModule;
import foundry.veil.api.quasar.particle.QuasarParticle;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class ConstantForceModule implements ForceParticleModule {

    private final Vector3d acceleration;
    private float strength;

    public ConstantForceModule(Vector3dc acceleration) {
        this(new Vector3d(acceleration));
    }

    public ConstantForceModule(Vector3d acceleration) {
        this.acceleration = acceleration;
        this.strength = 1.0F;
    }

    @Override
    public void applyForce(QuasarParticle particle) {
        particle.getVelocity().add(this.acceleration.x * this.strength, this.acceleration.y * this.strength, this.acceleration.z * this.strength);
    }

    @Override
    public void setStrength(float strength) {
        this.strength = strength;
    }

    public Vector3d getAcceleration() {
        return this.acceleration;
    }
}
