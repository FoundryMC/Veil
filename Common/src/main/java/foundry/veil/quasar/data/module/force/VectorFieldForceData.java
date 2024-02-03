package foundry.veil.quasar.data.module.force;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.modules.particle.update.fields.VectorField;
import foundry.veil.quasar.emitters.modules.particle.force.VectorFieldForceModule;

/**
 * <p>A force that applies the force created in a vector field to a particle.</p>
 * <p>Vector fields are useful for creating complex forces that vary over time.</p>
 *
 * @see VectorField
 */
public record VectorFieldForceData(VectorField vectorField,
                                   float strength,
                                   float falloff) implements ParticleModuleData {

    public static final Codec<VectorFieldForceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VectorField.CODEC.fieldOf("vector_field").forGetter(VectorFieldForceData::vectorField),
            Codec.FLOAT.fieldOf("strength").forGetter(VectorFieldForceData::strength),
            Codec.FLOAT.fieldOf("falloff").forGetter(VectorFieldForceData::falloff)
    ).apply(instance, VectorFieldForceData::new));

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule(new VectorFieldForceModule(this));
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.VECTOR_FIELD;
    }
}
