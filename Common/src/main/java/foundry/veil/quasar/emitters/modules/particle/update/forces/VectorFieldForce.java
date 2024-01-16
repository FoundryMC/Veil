package foundry.veil.quasar.emitters.modules.particle.update.forces;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.fields.VectorField;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.NotNull;

/**
 * A force that applies the force created in a vector field to a particle.
 * @see AbstractParticleForce
 * @see foundry.veil.quasar.emitters.modules.particle.update.UpdateModule
 * @see VectorField
 *
 * <p>
 *     Vector fields are useful for creating complex forces that vary over time.
 */
public class VectorFieldForce extends AbstractParticleForce {
    public static final Codec<VectorFieldForce> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    VectorField.CODEC.fieldOf("vector_field").forGetter(VectorFieldForce::getVectorField),
                    Codec.FLOAT.fieldOf("strength").forGetter(VectorFieldForce::getStrength),
                    Codec.FLOAT.fieldOf("falloff").forGetter(VectorFieldForce::getFalloff)
            ).apply(instance, VectorFieldForce::new)
            );
    private VectorField vectorField;
    public VectorField getVectorField() {
        return vectorField;
    }

    public VectorFieldForce(VectorField vectorField, float strength, float decay) {
        this.vectorField = vectorField;
        this.strength = strength;
        this.falloff = decay;
    }
    @Override
    public void applyForce(QuasarParticle particle) {
        particle.addForce(vectorField.getVector(particle.getPos()).scale(strength));
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.VECTOR_FIELD;
    }
    public ImBoolean shouldStay = new ImBoolean(true);

    @Override
    public boolean shouldRemove() {
        return !shouldStay.get();
    }


    @Override
    public VectorFieldForce copy() {
        return new VectorFieldForce(vectorField, strength, falloff);
    }

    
}
