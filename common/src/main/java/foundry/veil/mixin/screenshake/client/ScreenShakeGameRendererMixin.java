package foundry.veil.mixin.screenshake.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class ScreenShakeGameRendererMixin {

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getPosition()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 veil$moveCameraPosition(Camera instance, Operation<Vec3> original, @Local(name = "f") float partialTick) {
        Vector3f pos = VeilRenderSystem.renderer().getScreenShakeManager().getPosition(partialTick);
        return original.call(instance).add(pos.x, pos.y, pos.z);
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;rotation()Lorg/joml/Quaternionf;"))
    private Quaternionf veil$offsetCameraRotation(Camera instance, Operation<Quaternionf> original, @Local(name = "f") float partialTick) {
        Vector3f offset = VeilRenderSystem.renderer().getScreenShakeManager().getPosition(partialTick).mul(0.1f);
        return original.call(instance).rotateXYZ(offset.x, offset.y, offset.z, new Quaternionf());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void veil$tickScreenShake(CallbackInfo ci) {
        VeilRenderSystem.renderer().getScreenShakeManager().tick();
    }
}
