package foundry.veil.api.quasar.emitters.module.force;

import foundry.veil.api.quasar.emitters.module.ForceParticleModule;
import foundry.veil.api.quasar.particle.QuasarParticle;
import org.joml.Quaterniond;
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
        Vector3d rotatedAcceleration = this.acceleration.rotate(particle.getEmitter().getRotation().get(new Quaterniond()), new Vector3d());
        particle.getVelocity().add(rotatedAcceleration.x * this.strength, rotatedAcceleration.y * this.strength, rotatedAcceleration.z * this.strength);
    }

    @Override
    public void setStrength(float strength) {
        this.strength = strength;
    }

    public Vector3d getAcceleration() {
        return this.acceleration;
    }
}
