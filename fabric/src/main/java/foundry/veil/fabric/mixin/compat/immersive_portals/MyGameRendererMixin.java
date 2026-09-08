package foundry.veil.fabric.mixin.compat.immersive_portals;

import com.llamalad7.mixinextras.sugar.Local;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.fabric.compat.immptl.VeilFabricImmersivePortalsCompat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.render.MyGameRenderer;

import java.util.function.Consumer;

@Mixin(MyGameRenderer.class)
public class MyGameRendererMixin {

    @Shadow
    @Final
    public static Minecraft client;

    @Inject(method = "switchAndRenderTheWorld", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;applyModelViewMatrix()V", ordinal = 0))
    private static void veil$prePortalRender(ClientLevel newWorld, Vec3 thisTickCameraPos, Vec3 lastTickCameraPos, Consumer<Runnable> invokeWrapper, int renderDistance, boolean doRenderHand, CallbackInfo ci) {
        VeilFabricImmersivePortalsCompat.renderingPortal = true;
    }

    @Inject(method = "switchAndRenderTheWorld", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;applyModelViewMatrix()V", ordinal = 1))
    private static void veil$postPortalRender(ClientLevel newWorld, Vec3 thisTickCameraPos, Vec3 lastTickCameraPos, Consumer<Runnable> invokeWrapper, int renderDistance, boolean doRenderHand, CallbackInfo ci, @Local(name = "oldProjectionMatrix") Matrix4f oldProjectionMatrix, @Local(name = "oldCamera") Camera oldCamera) {
        VeilFabricImmersivePortalsCompat.renderingPortal = false;
        Quaternionf quaternionf = oldCamera.rotation().conjugate(new Quaternionf());
        Matrix4f matrix4f2 = (new Matrix4f()).rotation(quaternionf);
        VeilRenderSystem.renderer().getCameraMatrices().update(oldProjectionMatrix, matrix4f2, oldCamera.getPosition().x(), oldCamera.getPosition().y(), oldCamera.getPosition().z());
    }

}
