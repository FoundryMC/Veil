package foundry.veil.mixin.client.imgui;

import foundry.veil.imgui.VeilImGui;
import foundry.veil.render.pipeline.VeilRenderSystem;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("TAIL"))
    public void keyCallback(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        VeilImGui imGui = VeilRenderSystem.renderer().getImGui();
        if (imGui.getWindow() == window) {
            imGui.getImplGlfw().keyCallback(window, key, scancode, action, mods);
        }
    }

    @Inject(method = "charTyped", at = @At("TAIL"))
    public void charCallback(long window, int codepoint, int mods, CallbackInfo ci) {
        VeilImGui imGui = VeilRenderSystem.renderer().getImGui();
        if (imGui.getWindow() == window) {
            imGui.getImplGlfw().charCallback(window, codepoint);
        }
    }
}
