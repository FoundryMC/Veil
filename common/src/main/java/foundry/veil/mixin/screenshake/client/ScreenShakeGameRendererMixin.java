package foundry.veil.mixin.screenshake.client;

import com.llamalad7.mixinextras.sugar.Local;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class ScreenShakeGameRendererMixin {

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getPosition()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 veil$moveCameraPosition(Camera instance, @Local(name = "f") float partialTick) {
        return instance.getPosition().add(new Vec3(VeilRenderSystem.renderer().getScreenShakeManager().getPosition(partialTick)));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void veil$tickScreenShake(CallbackInfo ci) {
        VeilRenderSystem.renderer().getScreenShakeManager().tick();
    }
}
