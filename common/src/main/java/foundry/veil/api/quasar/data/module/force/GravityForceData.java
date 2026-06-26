package foundry.veil.api.quasar.data.module.force;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.force.ConstantForceModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import imgui.ImGui;
import org.joml.Vector3d;

/**
 * A force that applies a gravity force to a particle.
 */
public final class GravityForceData implements ParticleModuleData, EditorAttributeProvider {

    public static final MapCodec<GravityForceData> CODEC = Codec.DOUBLE.fieldOf("strength").xmap(GravityForceData::new, GravityForceData::strength);
    private double strength;

    public GravityForceData(double strength) {
        this.strength = strength;
    }

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule(new ConstantForceModule(new Vector3d(0, -this.strength / 400.0, 0)));
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.GRAVITY;
    }

    public double strength() {
        return strength;
    }

    @Override
    public void renderImGuiAttributes() {
        double[] editStrength = new double[]{this.strength};
        if (ImGui.dragScalar("strength", editStrength, 0.01F)) {
            this.strength = editStrength[0];
        }
    }
}
