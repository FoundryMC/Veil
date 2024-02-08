package foundry.veil.quasar.emitters.modules.particle.force;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.data.module.force.VectorFieldForceData;
import foundry.veil.quasar.emitters.modules.particle.ForceParticleModule;
import foundry.veil.quasar.emitters.modules.particle.update.fields.VectorField;
import org.joml.Vector3d;

public class VectorFieldForceModule implements ForceParticleModule {

    private VectorField vectorField;
    private float strength;

    private final Vector3d temp;

    public VectorFieldForceModule(VectorFieldForceData data) {
        this(data.vectorField(), data.strength());
    }

    public VectorFieldForceModule(VectorField vectorField, float strength) {
        this.vectorField = vectorField;
        this.strength = strength;
        this.temp = new Vector3d();
    }

    @Override
    public void applyForce(QuasarParticle particle) {
        Vector3d vector = this.vectorField.getVector(particle.getPosition(), this.temp);
        particle.getVelocity().add(vector.mul(this.strength));
    }

    public void setVectorField(VectorField vectorField) {
        this.vectorField = vectorField;
    }

    @Override
    public void setStrength(float strength) {
        this.strength = strength;
    }
}
