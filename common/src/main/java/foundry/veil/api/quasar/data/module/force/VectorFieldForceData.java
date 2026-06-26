package foundry.veil.api.quasar.data.module.force;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.force.VectorFieldForceModule;
import foundry.veil.api.quasar.emitters.module.update.VectorField;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import imgui.ImGui;

/**
 * <p>A force that applies the force created in a vector field to a particle.</p>
 * <p>Vector fields are useful for creating complex forces that vary over time.</p>
 *
 * @see VectorField
 */
public final class VectorFieldForceData implements ParticleModuleData, EditorAttributeProvider {

    public static final MapCodec<VectorFieldForceData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            VectorField.CODEC.fieldOf("vector_field").forGetter(VectorFieldForceData::vectorField),
            Codec.FLOAT.fieldOf("strength").forGetter(VectorFieldForceData::strength)
    ).apply(instance, VectorFieldForceData::new));
    private final VectorField vectorField;
    private float strength;

    public VectorFieldForceData(VectorField vectorField,
                                float strength) {
        this.vectorField = vectorField;
        this.strength = strength;
    }

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule(new VectorFieldForceModule(this));
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.VECTOR_FIELD;
    }

    @Override
    public void renderImGuiAttributes() {
        vectorField.renderImGuiAttributes();

        float[] editStrength = new float[]{this.strength};
        if (ImGui.dragScalar("strength", editStrength, 0.01F)) {
            this.strength = editStrength[0];
        }
    }

    public VectorField vectorField() {
        return vectorField;
    }

    public float strength() {
        return strength;
    }

}
