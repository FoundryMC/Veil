package foundry.veil.api.quasar.emitters.module.force;

import foundry.veil.api.quasar.emitters.module.ForceParticleModule;
import foundry.veil.api.quasar.particle.QuasarParticle;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class ScaleForceModule implements ForceParticleModule {

    private final Vector3d scale;
    private float strength;

    public ScaleForceModule(Vector3dc scale) {
        this.scale = new Vector3d(scale);
        this.strength = 1.0F;
    }

    public ScaleForceModule(double scale) {
        this.scale = new Vector3d(scale);
        this.strength = 1.0F;
    }

    @Override
    public void applyForce(QuasarParticle particle) {
        particle.getVelocity().mul(
                1.0 + (this.scale.x - 1.0) * this.strength,
                1.0 + (this.scale.y - 1.0) * this.strength,
                1.0 + (this.scale.z - 1.0) * this.strength);
    }

    @Override
    public void setStrength(float strength) {
        this.strength = strength;
    }

    public Vector3d getScale() {
        return this.scale;
    }
}
