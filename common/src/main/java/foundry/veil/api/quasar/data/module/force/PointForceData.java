package foundry.veil.api.quasar.data.module.force;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.force.PointForceModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import foundry.veil.api.util.CodecUtil;
import imgui.ImGui;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * A point force is used to apply a force in the direction away from a point.
 */
public final class PointForceData implements ParticleModuleData, EditorAttributeProvider {

    public static final MapCodec<PointForceData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CodecUtil.VECTOR3DC_CODEC.optionalFieldOf("point", new Vector3d(0, 0, 0)).forGetter(PointForceData::point),
            Codec.BOOL.optionalFieldOf("localPoint", false).forGetter(PointForceData::localPoint),
            Codec.FLOAT.fieldOf("range").forGetter(PointForceData::range),
            Codec.FLOAT.fieldOf("strength").forGetter(PointForceData::strength)
    ).apply(instance, PointForceData::new));
    private Vector3dc point;
    private boolean localPoint;
    private float range;
    private float strength;

    public PointForceData(Vector3dc point,
                          boolean localPoint,
                          float range,
                          float strength) {
        this.point = point;
        this.localPoint = localPoint;
        this.range = range;
        this.strength = strength;
    }

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule(new PointForceModule(this));
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.POINT;
    }

    public Vector3dc point() {
        return point;
    }

    public boolean localPoint() {
        return localPoint;
    }

    public float range() {
        return range;
    }

    public float strength() {
        return strength;
    }

    @Override
    public void renderImGuiAttributes() {
        float[] editPos = new float[]{(float) point.x(), (float) point.y(), (float) point.z()};

        if (ImGui.dragFloat3("point", editPos, 0.01F)) {
            this.point = new Vector3d(editPos[0], editPos[1], editPos[2]);
        }

        if (ImGui.checkbox("local_point", localPoint)) {
            this.localPoint = !this.localPoint;
        }

        float[] editRange = new float[] {range};

        if (ImGui.dragScalar("range", editRange, 0.01F)) {
            this.range = editRange[0];
        }

        float[] editStrength = new float[] {strength};

        if (ImGui.dragScalar("strength", editStrength, 0.01F)) {
            this.strength = editStrength[0];
        }
    }
}
