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
import foundry.veil.impl.client.editor.ParticleEditorInspector;
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

    /**
     *
     */
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
        float[] editAxisX = new float[]{(float) vortexAxis.x()};
        float[] editAxisY = new float[]{(float) vortexAxis.y()};
        float[] editAxisZ = new float[]{(float) vortexAxis.z()};

        if (ParticleEditorInspector.vec3Field("vortex_center", editAxisX, editAxisY, editAxisZ, 0.01F)) {
            this.vortexAxis = new Vector3d(editAxisX[0], editAxisY[0], editAxisZ[0]);
        }

        float[] editX = new float[]{(float) vortexCenter.x()};
        float[] editY = new float[]{(float) vortexCenter.y()};
        float[] editZ = new float[]{(float) vortexCenter.z()};

        if (ParticleEditorInspector.vec3Field("vortex_center", editX, editY, editZ, 0.01F)) {
            this.vortexCenter = new Vector3d(editX[0], editY[0], editZ[0]);
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
