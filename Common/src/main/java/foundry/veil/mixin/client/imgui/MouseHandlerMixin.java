package foundry.veil.mixin.client.imgui;

import foundry.veil.imgui.VeilImGuiImpl;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    public void mouseButtonCallback(long window, int button, int action, int mods, CallbackInfo ci) {
        if (VeilImGuiImpl.get().mouseButtonCallback(window, button, action, mods)) {
            ci.cancel();
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    public void scrollCallback(long window, double xOffset, double yOffset, CallbackInfo ci) {
        if (VeilImGuiImpl.get().scrollCallback(window, xOffset, yOffset)) {
            ci.cancel();
        }
    }

    @Inject(method = "grabMouse", at = @At("HEAD"))
    public void grabMouse(CallbackInfo ci) {
        VeilImGuiImpl.get().onGrabMouse();
    }

    @Inject(method = "xpos", at = @At("HEAD"), cancellable = true)
    public void cancelMouseX(CallbackInfoReturnable<Double> cir) {
        if (VeilImGuiImpl.get().shouldHideMouse()) {
            cir.setReturnValue(Double.MIN_VALUE);
        }
    }

    @Inject(method = "ypos", at = @At("HEAD"), cancellable = true)
    public void cancelMouseY(CallbackInfoReturnable<Double> cir) {
        if (VeilImGuiImpl.get().shouldHideMouse()) {
            cir.setReturnValue(Double.MIN_VALUE);
        }
    }
}
