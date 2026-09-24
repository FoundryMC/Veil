package foundry.veil.mixin.screenshake.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
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

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V"))
    private void veil$capturePartialTicks(CallbackInfo ci, @Local(argsOnly = true) DeltaTracker deltaTracker, @Share("partialTicks") LocalFloatRef partialTicksRef) {
        partialTicksRef.set(deltaTracker.getGameTimeDeltaPartialTick(false));
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getPosition()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 veil$moveCameraPosition(Camera instance, Operation<Vec3> original, @Share("partialTicks") LocalFloatRef partialTicksRef) {
        Vector3f pos = VeilRenderSystem.renderer().getScreenShakeManager().getPosition(partialTicksRef.get());
        return original.call(instance).add(pos.x, pos.y, pos.z);
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;rotation()Lorg/joml/Quaternionf;"))
    private Quaternionf veil$offsetCameraRotation(Camera instance, Operation<Quaternionf> original, @Share("partialTicks") LocalFloatRef partialTicksRef) {
        Vector3f offset = VeilRenderSystem.renderer().getScreenShakeManager().getPosition(partialTicksRef.get()).mul(0.1f);
        return original.call(instance).rotateXYZ(offset.x, offset.y, offset.z, new Quaternionf());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void veil$tickScreenShake(CallbackInfo ci) {
        VeilRenderSystem.renderer().getScreenShakeManager().tick();
    }
}
