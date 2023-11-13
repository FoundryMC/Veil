package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import imgui.ImGui;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class FaceDirectionModule implements RenderModule {
    Vec3 direction;

    public FaceDirectionModule(Vec3 direction) {
        this.direction = direction;
    }
    @Override
    public void apply(QuasarParticle particle, float partialTicks, RenderData data) {
        // calculate pitch yaw and roll from current pitch yaw and roll to direction
        float currentPitch = data.getPitch();
        float currentYaw = data.getYaw();
        float currentRoll = data.getRoll();
        Vec3 targetDirection = direction.normalize();
        float targetPitch = (float) Math.asin(targetDirection.y());
        float targetYaw = (float) Math.atan2(targetDirection.x(), targetDirection.z());
        float targetRoll = 0;
        float pitch = currentPitch + (targetPitch - currentPitch) * partialTicks;
        float yaw = currentYaw + (targetYaw - currentYaw) * partialTicks;
        float roll = currentRoll + (targetRoll - currentRoll) * partialTicks;
        data.setRotation(pitch, yaw, roll);
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return null;
    }

    @Override
    public void renderImGuiSettings() {
        float[] directionArray = new float[] {(float) direction.x, (float) direction.y, (float) direction.z};
        ImGui.dragFloat3("Direction" + this.hashCode(), directionArray);
        direction = new Vec3(directionArray[0], directionArray[1], directionArray[2]);
    }
}
