package foundry.veil.fabric.mixin.compat.flashback;

import com.moulberry.flashback.exporting.ExportJob;
import foundry.veil.api.client.render.VeilRenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExportJob.class)
public class ExportJobMixin {

    @Inject(method = "doExport", at = @At(value = "INVOKE", target = "Lcom/moulberry/flashback/exporting/ExportJob;updateClientFreeze(Z)V", shift = At.Shift.BEFORE), remap = false)
    public void beginFrame(CallbackInfo ci) {
        VeilRenderSystem.beginFrame();
    }

    @Inject(method = "doExport", at = @At(value = "INVOKE", target = "Lcom/moulberry/flashback/exporting/ExportJob;submitDownloadedFrames(Lcom/moulberry/flashback/exporting/VideoWriter;Lcom/moulberry/flashback/exporting/SaveableFramebufferQueue;Z)V", ordinal = 0, shift = At.Shift.AFTER), remap = false)
    public void endFrame(CallbackInfo ci) {
        VeilRenderSystem.endFrame();
    }

}