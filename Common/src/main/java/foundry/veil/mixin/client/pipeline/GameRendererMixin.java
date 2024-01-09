package foundry.veil.mixin.client.pipeline;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.render.pipeline.VeilFirstPersonRenderer;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.pipeline.VeilRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "resize", at = @At(value = "HEAD"))
    public void veil$resizeListener(int pWidth, int pHeight, CallbackInfo ci) {
        VeilRenderSystem.resize(pWidth, pHeight);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.BEFORE))
    public void veil$renderPost(float partialTicks, long time, boolean renderLevel, CallbackInfo ci) {
        VeilRenderSystem.renderPost(partialTicks);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Lighting;setupFor3DItems()V", shift = At.Shift.AFTER))
    public void veil$updateGuiCamera(float partialTicks, long time, boolean renderLevel, CallbackInfo ci) {
        VeilRenderer renderer = VeilRenderSystem.renderer();
        renderer.getCameraMatrices().updateGui();
        renderer.getGuiInfo().update();
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void veil$unbindGuiCamera(float partialTicks, long time, boolean renderLevel, CallbackInfo ci) {
        VeilRenderSystem.renderer().getGuiInfo().unbind();
    }

    @Inject(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LightTexture;turnOnLightLayer()V", ordinal = 0, shift = At.Shift.BEFORE))
    public void veil$preDrawFirstPerson(PoseStack $$0, Camera $$1, float $$2, CallbackInfo ci) {
        VeilFirstPersonRenderer.setup();
    }

    @Inject(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LightTexture;turnOffLightLayer()V", ordinal = 0, shift = At.Shift.AFTER))
    public void veil$postDrawFirstPerson(PoseStack $$0, Camera $$1, float $$2, CallbackInfo ci) {
        VeilFirstPersonRenderer.blit();
    }
}