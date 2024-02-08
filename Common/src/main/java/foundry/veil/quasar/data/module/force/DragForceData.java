package foundry.veil.quasar.data.module.force;

import com.mojang.serialization.Codec;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.modules.particle.force.ConstantForceModule;
import foundry.veil.quasar.emitters.modules.particle.force.ScaleForceModule;
import org.joml.Vector3d;

/**
 * A force that applies a drag force to a particle.
 *
 * <p>
 * Drag forces are forces that are applied in the opposite direction of the particle's velocity.
 * They are useful for simulating air resistance.
 * The strength of the force is determined by the strength parameter.
 */
public record DragForceData(double strength) implements ParticleModuleData {

    public static final Codec<DragForceData> CODEC = Codec.DOUBLE.fieldOf("strength").xmap(DragForceData::new, DragForceData::strength).codec();

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule(new ScaleForceModule(this.strength));
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.DRAG;
    }
}
