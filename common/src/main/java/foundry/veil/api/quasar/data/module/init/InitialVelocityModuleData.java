package foundry.veil.api.quasar.data.module.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.InitParticleModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import foundry.veil.api.util.CodecUtil;
import imgui.ImGui;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class InitialVelocityModuleData implements ParticleModuleData, EditorAttributeProvider {

    public static final MapCodec<InitialVelocityModuleData> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    CodecUtil.VECTOR3DC_CODEC.fieldOf("direction").forGetter(InitialVelocityModuleData::velocityDirection),
                    Codec.BOOL.fieldOf("take_parent_rotation").orElse(true).forGetter(InitialVelocityModuleData::takesParentRotation),
                    Codec.FLOAT.fieldOf("strength").forGetter(InitialVelocityModuleData::strength)
            ).apply(instance, InitialVelocityModuleData::new));
    private Vector3dc velocityDirection;
    private boolean takesParentRotation;
    private float strength;

    public InitialVelocityModuleData(Vector3dc velocityDirection,
                                     boolean takesParentRotation,
                                     float strength) {
        this.velocityDirection = velocityDirection;
        this.takesParentRotation = takesParentRotation;
        this.strength = strength;
    }

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        // TODO takesParentRotation
        builder.addModule((InitParticleModule) particle -> particle.getVelocity().add(this.velocityDirection.normalize(this.strength, new Vector3d())));
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.INITIAL_VELOCITY;
    }

    @Override
    public void renderImGuiAttributes() {
        float[] editDirection = new float[]{(float) velocityDirection.x(), (float) velocityDirection.y(), (float) velocityDirection.z()};

        if (ImGui.dragFloat3("direction", editDirection, 0.01F)) {
            this.velocityDirection = new Vector3d(editDirection[0], editDirection[1], editDirection[2]);
        }

        if (ImGui.checkbox("takes_parent_rotation", takesParentRotation)) {
            this.takesParentRotation = !this.takesParentRotation;
        }

        float[] editStrength = new float[] {strength};

        if (ImGui.dragScalar("strength", editStrength, 0.01F)) {
            this.strength = editStrength[0];
        }
    }

    public Vector3dc velocityDirection() {
        return velocityDirection;
    }

    public boolean takesParentRotation() {
        return takesParentRotation;
    }

    public float strength() {
        return strength;
    }
}
