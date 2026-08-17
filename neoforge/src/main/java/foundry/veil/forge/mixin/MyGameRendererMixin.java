package foundry.veil.forge.mixin;

import foundry.veil.forge.compat.immptl.VeilForgeImmersivePortalsCompat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.render.MyGameRenderer;

import java.util.function.Consumer;

@Mixin(MyGameRenderer.class)
public class MyGameRendererMixin {

    @Inject(method = "switchAndRenderTheWorld", at = @At(value = "INVOKE", target = "Lqouteall/imm_ptl/core/ducks/IEMinecraftClient;ip_setWorldRenderer(Lnet/minecraft/client/renderer/LevelRenderer;)V", ordinal = 0))
    private static void veil$prePortalRender(ClientLevel newWorld, Vec3 thisTickCameraPos, Vec3 lastTickCameraPos, Consumer<Runnable> invokeWrapper, int renderDistance, boolean doRenderHand, CallbackInfo ci) {
        VeilForgeImmersivePortalsCompat.renderingPortal = true;
    }

    @Inject(method = "switchAndRenderTheWorld", at = @At(value = "INVOKE", target = "Lqouteall/imm_ptl/core/ducks/IEMinecraftClient;ip_setWorldRenderer(Lnet/minecraft/client/renderer/LevelRenderer;)V", ordinal = 1))
    private static void veil$postPortalRender(ClientLevel newWorld, Vec3 thisTickCameraPos, Vec3 lastTickCameraPos, Consumer<Runnable> invokeWrapper, int renderDistance, boolean doRenderHand, CallbackInfo ci) {
        VeilForgeImmersivePortalsCompat.renderingPortal = false;
    }

}
