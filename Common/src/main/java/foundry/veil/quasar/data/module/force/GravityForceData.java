package foundry.veil.quasar.data.module.force;

import com.mojang.serialization.Codec;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.modules.particle.force.ConstantForceModule;
import org.joml.Vector3d;

/**
 * A force that applies a gravity force to a particle.
 */
public record GravityForceData(double strength) implements ParticleModuleData {

    public static final Codec<GravityForceData> CODEC = Codec.DOUBLE.fieldOf("strength").xmap(GravityForceData::new, GravityForceData::strength).codec();

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule(new ConstantForceModule(new Vector3d(0, -this.strength, 0)));
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.GRAVITY;
    }
}
