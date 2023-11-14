package foundry.veil.mixin.client.imgui;

import foundry.veil.imgui.VeilImGuiImpl;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onPress", at = @At("TAIL"))
    public void mouseButtonCallback(long window, int button, int action, int mods, CallbackInfo ci) {
        VeilImGuiImpl.get().mouseButtonCallback(window, button, action, mods);
    }

    @Inject(method = "onScroll", at = @At("TAIL"))
    public void scrollCallback(long window, double xOffset, double yOffset, CallbackInfo ci) {
        VeilImGuiImpl.get().scrollCallback(window, xOffset, yOffset);
    }
}
