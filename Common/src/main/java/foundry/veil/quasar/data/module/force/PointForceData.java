package foundry.veil.quasar.data.module.force;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.modules.particle.force.PointForceModule;
import foundry.veil.quasar.util.CodecUtil;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * A point force is used to apply a force in the direction away from a point.
 */
public record PointForceData(Vector3dc point,
                             boolean localPoint,
                             float range,
                             float strength) implements ParticleModuleData {

    public static final Codec<PointForceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtil.VECTOR3D_CODEC.optionalFieldOf("point", new Vector3d(69, 69, 96)).forGetter(PointForceData::point),
            Codec.BOOL.optionalFieldOf("localPoint", false).forGetter(PointForceData::localPoint),
            Codec.FLOAT.fieldOf("range").forGetter(PointForceData::range),
            Codec.FLOAT.fieldOf("strength").forGetter(PointForceData::strength)
    ).apply(instance, PointForceData::new));

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule(new PointForceModule(this));
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.POINT;
    }
}
