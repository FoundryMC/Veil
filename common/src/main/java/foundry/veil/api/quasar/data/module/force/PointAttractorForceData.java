package foundry.veil.api.quasar.data.module.force;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.force.PointAttractorForceModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import foundry.veil.api.util.CodecUtil;
import imgui.ImGui;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * A force that attracts particles to a point.
 *
 * <p>
 * Point attractor forces are forces that are applied in the direction of a point.
 * They are useful for simulating gravity or other forces that pull particles towards a point.
 * The strength of the force is determined by the strength parameter.
 * The falloff parameter determines how quickly the force falls off with distance. (unused)
 * The strengthByDistance parameter determines whether the strength of the force is multiplied by the distance from the point.
 * If strengthByDistance is true, the strength of the force is multiplied by (1 - distance / range).
 * If strengthByDistance is false, the strength of the force is not affected by distance.
 * The range parameter determines the maximum distance from the point at which the force is applied.
 * If the distance from the point is greater than the range, the force is not applied.
 * The position parameter determines the position of the point.
 * The position parameter can be a Vec3 or a Supplier Vec3.
 * If the position parameter is a Vec3, the position of the point is fixed.
 * If the position parameter is a Supplier Vec3, the position of the point is updated every tick.
 * This allows the point to move.
 * </p>
 */
public final class PointAttractorForceData implements ParticleModuleData, EditorAttributeProvider {

    public static final MapCodec<PointAttractorForceData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CodecUtil.VECTOR3DC_CODEC.fieldOf("position").forGetter(PointAttractorForceData::position),
            Codec.BOOL.optionalFieldOf("localPosition", false).forGetter(PointAttractorForceData::invertDistanceModifier),
            Codec.FLOAT.fieldOf("range").forGetter(PointAttractorForceData::range),
            Codec.FLOAT.fieldOf("strength").forGetter(PointAttractorForceData::strength),
            Codec.BOOL.fieldOf("strengthByDistance").forGetter(PointAttractorForceData::strengthByDistance),
            Codec.BOOL.optionalFieldOf("invertDistanceModifier", false).forGetter(PointAttractorForceData::invertDistanceModifier)
    ).apply(instance, PointAttractorForceData::new));
    private Vector3dc position;
    private boolean localPosition;
    private float range;
    private float strength;
    private boolean strengthByDistance;
    private boolean invertDistanceModifier;

    public PointAttractorForceData(Vector3dc position,
                                   boolean localPosition,
                                   float range,
                                   float strength,
                                   boolean strengthByDistance,
                                   boolean invertDistanceModifier) {
        this.position = position;
        this.localPosition = localPosition;
        this.range = range;
        this.strength = strength;
        this.strengthByDistance = strengthByDistance;
        this.invertDistanceModifier = invertDistanceModifier;
    }

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule(new PointAttractorForceModule(this));
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.POINT_ATTRACTOR;
    }

    @Override
    public void renderImGuiAttributes() {
        float[] editPos = new float[]{(float) position.x(), (float) position.y(), (float) position.z()};

        if (ImGui.dragFloat3("position", editPos, 0.01F)) {
            this.position = new Vector3d(editPos[0], editPos[1], editPos[2]);
        }

        if (ImGui.checkbox("local_position", localPosition)) {
            this.localPosition = !this.localPosition;
        }

        float[] editRange = new float[] {range};

        if (ImGui.dragScalar("range", editRange, 0.01F)) {
            this.range = editRange[0];
        }

        float[] editStrength = new float[] {strength};

        if (ImGui.dragScalar("strength", editStrength, 0.01F)) {
            this.strength = editStrength[0];
        }

        if (ImGui.checkbox("strength_by_distance", strengthByDistance)) {
            this.strengthByDistance = !this.strengthByDistance;
        }

        if (ImGui.checkbox("invert_distance_modifier", invertDistanceModifier)) {
            this.invertDistanceModifier = !this.invertDistanceModifier;
        }
    }

    public Vector3dc position() {
        return position;
    }

    public boolean localPosition() {
        return localPosition;
    }

    public float range() {
        return range;
    }

    public float strength() {
        return strength;
    }

    public boolean strengthByDistance() {
        return strengthByDistance;
    }

    public boolean invertDistanceModifier() {
        return invertDistanceModifier;
    }

}
