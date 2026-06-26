package foundry.veil.api.quasar.data.module.force;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.force.VortexForceModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import foundry.veil.api.util.CodecUtil;
import imgui.ImGui;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * A force that applies a vortex force to a particle.
 *
 * <p>
 * Vortex forces are forces that are applied in a circular motion around a center point.
 * They are useful for simulating whirlpools or tornadoes.
 * The strength of the force is determined by the strength parameter.
 * The falloff parameter determines how quickly the force falls off with distance. (unused)
 */
public final class VortexForceData implements ParticleModuleData, EditorAttributeProvider {

    public static final MapCodec<VortexForceData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CodecUtil.VECTOR3DC_CODEC.fieldOf("vortex_axis").forGetter(VortexForceData::vortexAxis),
            CodecUtil.VECTOR3DC_CODEC.fieldOf("vortex_center").forGetter(VortexForceData::vortexCenter),
            Codec.BOOL.optionalFieldOf("local_position", false).forGetter(VortexForceData::localPosition),
            Codec.DOUBLE.fieldOf("range").forGetter(VortexForceData::range),
            Codec.FLOAT.fieldOf("strength").forGetter(VortexForceData::strength)
    ).apply(instance, VortexForceData::new));
    private Vector3dc vortexAxis;
    private Vector3dc vortexCenter;
    private boolean localPosition;
    private double range;
    private float strength;

    public VortexForceData(Vector3dc vortexAxis,
                           Vector3dc vortexCenter,
                           boolean localPosition,
                           double range,
                           float strength) {
        this.vortexAxis = vortexAxis;
        this.vortexCenter = vortexCenter;
        this.localPosition = localPosition;
        this.range = range;
        this.strength = strength;
    }

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule(new VortexForceModule(this));
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.VORTEX;
    }

    @Override
    public void renderImGuiAttributes() {
        float[] editAxis = new float[]{(float) vortexAxis.x(), (float) vortexAxis.y(), (float) vortexAxis.z()};

        if (ImGui.dragFloat3("vortex_axis", editAxis, 0.01F)) {
            this.vortexAxis = new Vector3d(editAxis[0], editAxis[1], editAxis[2]);
        }

        float[] editPos = new float[]{(float) vortexCenter.x(), (float) vortexCenter.y(), (float) vortexCenter.z()};

        if (ImGui.dragFloat3("vortex_center", editPos, 0.01F)) {
            this.vortexCenter = new Vector3d(editPos[0], editPos[1], editPos[2]);
        }

        if (ImGui.checkbox("local_position", localPosition)) {
            this.localPosition = !this.localPosition;
        }

        double[] editRange = new double[] {range};

        if (ImGui.dragScalar("range", editRange, 0.01F)) {
            this.range = editRange[0];
        }

        float[] editStrength = new float[] {strength};

        if (ImGui.dragScalar("strength", editStrength, 0.01F)) {
            this.strength = editStrength[0];
        }
    }

    public Vector3dc vortexAxis() {
        return vortexAxis;
    }

    public Vector3dc vortexCenter() {
        return vortexCenter;
    }

    public boolean localPosition() {
        return localPosition;
    }

    public double range() {
        return range;
    }

    public float strength() {
        return strength;
    }

}
