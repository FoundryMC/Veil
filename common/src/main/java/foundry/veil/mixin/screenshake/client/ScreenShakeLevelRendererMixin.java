package foundry.veil.mixin.screenshake.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class ScreenShakeLevelRendererMixin {

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getPosition()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 veil$moveCameraPosition(Camera instance, Operation<Vec3> original, @Local(name = "f") float partialTick) {
        Vector3f pos = VeilRenderSystem.renderer().getScreenShakeManager().getPosition(partialTick);
        return original.call(instance).add(pos.x, pos.y, pos.z);
    }
}
