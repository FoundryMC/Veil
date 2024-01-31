package foundry.veil.quasar.emitters.modules.particle.update.rotation;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3fc;

public class FaceDirectionParticleModule implements UpdateParticleModule {

    private final Vector3dc direction;

    public FaceDirectionParticleModule(Vector3dc direction) {
        this.direction = direction.normalize(new Vector3d());
    }

    @Override
    public void run(QuasarVanillaParticle particle) {
        // calculate pitch yaw and roll from current pitch yaw and roll to direction
//        Vector3fc rotation = particle.getRotation();
//        float currentPitch = rotation.x();
//        float currentYaw = rotation.y();
//        float currentRoll = rotation.z();
        float targetPitch = (float) Math.asin(this.direction.y());
        float targetYaw = (float) Math.atan2(this.direction.x(), this.direction.z());
        float targetRoll = 0;
//        float pitch = currentPitch + (targetPitch - currentPitch) * partialTicks;
//        float yaw = currentYaw + (targetYaw - currentYaw) * partialTicks;
//        float roll = currentRoll + (targetRoll - currentRoll) * partialTicks;
        particle.setRotation(targetPitch, targetYaw, 0);
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return null;
    }

}
