package foundry.veil.mixin.debug.client.profiler;

import foundry.veil.api.client.render.profiler.RenderProfilerCounter;
import foundry.veil.api.client.render.profiler.VeilRenderProfiler;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", args = "ldc=level"))
    public void preRenderLevel(CallbackInfo ci) {
        VeilRenderProfiler.get().push("level", RenderProfilerCounter.STANDARD_GEOMETRY);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V"))
    public void preRenderOutline(CallbackInfo ci) {
        VeilRenderProfiler.get().popPush("entity_outline", RenderProfilerCounter.FRAGMENT_SHADER_INVOCATIONS);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;process(F)V"))
    public void preRunPostEffect(CallbackInfo ci) {
        VeilRenderProfiler.get().popPush("entity_outline", RenderProfilerCounter.FRAGMENT_SHADER_INVOCATIONS);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;bindWrite(Z)V"))
    public void postRender(CallbackInfo ci) {
        VeilRenderProfiler.get().pop();
    }

    @Inject(method = "render", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=gui"))
    public void preRenderGui(CallbackInfo ci) {
        VeilRenderProfiler.get().push("hud", RenderProfilerCounter.STANDARD_GEOMETRY);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER))
    public void postRenderGui(CallbackInfo ci) {
        VeilRenderProfiler.get().pop();
    }
}
