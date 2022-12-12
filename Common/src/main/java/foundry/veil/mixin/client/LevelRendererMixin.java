package foundry.veil.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import foundry.veil.postprocessing.PostProcessingHandler;
import foundry.veil.shader.RenderTargetRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow @Final private Minecraft minecraft;
    @Unique
    @Nullable
    private RenderTarget veilCustomRenderTarget;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "net.minecraft.client.renderer.PostChain.process(F)V", ordinal = 1))
    public void injectionBeforeTransparencyChainProcess(CallbackInfo ci) {
        PostProcessingHandler.copyDepth();
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void injectionAfterLevelRendererConstructor(Minecraft $$0, EntityRenderDispatcher $$1, BlockEntityRenderDispatcher $$2, RenderBuffers $$3, CallbackInfo ci) {
    }

    // TODO: create a new PostChain for each render target, take in shader as parameter
    public void initCustomRenderTargets(){
        //PostChain chain = new PostChain(this.minecraft.getTextureManager(), this.minecraft.getMainRenderTarget(), this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
    }

    @Inject(method = "initTransparency", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;getTempTarget(Ljava/lang/String;)Lcom/mojang/blaze3d/pipeline/RenderTarget;"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void veil$injectCustomRenderTargets(CallbackInfo ci, ResourceLocation $$0, PostChain $$1) {
        for (String id : RenderTargetRegistry.getRenderTargets().keySet()) {
            RenderTargetRegistry.renderTargets.replace(id, $$1.getTempTarget(id));
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
    public void veil$injectCustomRenderTargets(CallbackInfo ci) {
        for (String id : RenderTargetRegistry.shouldCopyDepth) {
            RenderTarget target = RenderTargetRegistry.renderTargets.get(id);
            if (target == null) return;
            target.clear(Minecraft.ON_OSX);
            target.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    }

    @Inject(method = "deinitTransparency", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;destroyBuffers()V"))
    public void veil$deinitCustomRenderTargets(CallbackInfo ci) {
        for (RenderTarget target : RenderTargetRegistry.renderTargets.values()) {
            if (target == null) return;
            target.destroyBuffers();
            target = null;
        }
    }
}