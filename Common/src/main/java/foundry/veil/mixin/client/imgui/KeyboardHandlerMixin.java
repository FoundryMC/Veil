package foundry.veil.mixin.client.imgui;

import foundry.veil.impl.client.VeilImGuiImpl;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    public void keyCallback(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        if (VeilImGuiImpl.get().keyCallback(window, key, scancode, action, mods)) {
            ci.cancel();
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    public void charCallback(long window, int codepoint, int mods, CallbackInfo ci) {
        if (VeilImGuiImpl.get().charCallback(window, codepoint)) {
            ci.cancel();
        }
    }
}
