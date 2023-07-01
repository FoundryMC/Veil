package foundry.veil.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.pipeline.VeilFirstPerson;
import foundry.veil.pipeline.VeilRenderSystem;
import foundry.veil.postprocessing.PostProcessingHandler;
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
        PostProcessingHandler.resize(pWidth, pHeight);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;tryTakeScreenshotIfNeeded()V", shift = At.Shift.BEFORE))
    public void veil$renderPost(float partialTicks, long time, boolean renderLevel, CallbackInfo ci) {
        VeilRenderSystem.renderPost(partialTicks);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;F)V", shift = At.Shift.BEFORE))
    public void veil$updateGuiCamera(float partialTicks, long time, boolean renderLevel, CallbackInfo ci) {
        VeilRenderSystem.renderer().getCameraMatrices().updateGui();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V", shift = At.Shift.BEFORE))
    public void veil$setupFirstPerson(float $$0, long $$1, PoseStack $$2, CallbackInfo ci) {
        VeilFirstPerson.setup();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/Camera;F)V", shift = At.Shift.AFTER))
    public void veil$drawsetupFirstPerson(float $$0, long $$1, PoseStack $$2, CallbackInfo ci) {
        VeilFirstPerson.blit();
    }
}