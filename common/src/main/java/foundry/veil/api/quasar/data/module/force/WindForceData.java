package foundry.veil.api.quasar.data.module.force;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.force.ConstantForceModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import foundry.veil.api.util.CodecUtil;
import imgui.ImGui;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * A force that applies a wind force to a particle.
 *
 * <p>
 * Wind forces are useful for simulating wind.
 * The strength of the force is determined by the strength parameter.
 * The falloff parameter is unused.
 * The direction and speed of the wind is determined by the windDirection and windSpeed parameters.
 * The windDirection parameter is a vector that determines the direction of the wind.
 * The windSpeed parameter determines the speed of the wind.
 * The windSpeed parameter is measured in blocks/tick^2.
 */
public final class WindForceData implements ParticleModuleData, EditorAttributeProvider {

    public static final MapCodec<WindForceData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CodecUtil.VECTOR3DC_CODEC.fieldOf("wind_direction").forGetter(WindForceData::windDirection),
            Codec.FLOAT.fieldOf("wind_speed").forGetter(WindForceData::windSpeed),
            Codec.FLOAT.fieldOf("strength").forGetter(WindForceData::strength)
    ).apply(instance, WindForceData::new));
    private Vector3dc windDirection;
    private float windSpeed;
    private float strength;

    public WindForceData(Vector3dc windDirection,
                         float windSpeed,
                         float strength) {
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.strength = strength;
    }

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule(new ConstantForceModule(this.windDirection.normalize(this.windSpeed, new Vector3d())));
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.WIND;
    }

    @Override
    public void renderImGuiAttributes() {
        float[] editDirection = new float[]{(float) windDirection.x(), (float) windDirection.y(), (float) windDirection.z()};

        if (ImGui.dragFloat3("wind_direction", editDirection, 0.01F)) {
            this.windDirection = new Vector3d(editDirection[0], editDirection[1], editDirection[2]);
        }

        float[] editRange = new float[] {windSpeed};

        if (ImGui.dragScalar("wind_speed", editRange, 0.01F)) {
            this.windSpeed = editRange[0];
        }

        float[] editStrength = new float[] {strength};

        if (ImGui.dragScalar("strength", editStrength, 0.01F)) {
            this.strength = editStrength[0];
        }
    }

    public Vector3dc windDirection() {
        return windDirection;
    }

    public float windSpeed() {
        return windSpeed;
    }

    public float strength() {
        return strength;
    }
}
