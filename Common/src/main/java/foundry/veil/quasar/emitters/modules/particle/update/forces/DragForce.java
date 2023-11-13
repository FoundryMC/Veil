package foundry.veil.quasar.emitters.modules.particle.update.forces;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.mojang.serialization.Codec;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.NotNull;

/**
 * A force that applies a drag force to a particle.
 * @see AbstractParticleForce
 * @see foundry.veil.quasar.emitters.modules.particle.update.UpdateModule
 * <p>
 *     Drag forces are forces that are applied in the opposite direction of the particle's velocity.
 *     They are useful for simulating air resistance.
 *     The strength of the force is determined by the strength parameter.
 *     The falloff parameter is unused.
 */
public class DragForce extends AbstractParticleForce {
    public static final Codec<DragForce> CODEC = Codec.FLOAT.fieldOf("strength").xmap(DragForce::new, DragForce::getStrength).codec();
    public DragForce(float strength) {
        this.strength = strength;
    }
    @Override
    public void applyForce(QuasarParticle particle) {
        particle.modifyForce(strength);
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.DRAG;
    }
    public ImBoolean shouldStay = new ImBoolean(true);

    @Override
    public void renderImGuiSettings() {
        if(ImGui.collapsingHeader("Drag Force #" + this.hashCode(), shouldStay)){
            ImGui.text("Drag Force");
            float[] strength = new float[]{this.strength};
            ImGui.text("Strength");
            ImGui.sameLine();
            ImGui.dragFloat("##Strength " + this.hashCode(), strength);
            this.strength = strength[0];
        }
    }

    @Override
    public DragForce copy() {
        return new DragForce(strength);
    }

    
}
