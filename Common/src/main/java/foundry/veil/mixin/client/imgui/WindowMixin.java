package foundry.veil.mixin.client.imgui;

import com.mojang.blaze3d.platform.Window;
import foundry.veil.imgui.VeilImGui;
import foundry.veil.render.pipeline.VeilRenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {

    @Inject(method = "onFocus", at = @At("TAIL"))
    public void windowFocusCallback(long window, boolean focused, CallbackInfo ci) {
        VeilImGui imGui = VeilRenderSystem.renderer().getImGui();
        if (imGui.getWindow() == window) {
            imGui.getImplGlfw().windowFocusCallback(window, focused);
        }
    }

    @Inject(method = "onEnter", at=@At("TAIL"))
    public void cursorEnterCallback(long window, boolean entered, CallbackInfo ci){
        VeilImGui imGui = VeilRenderSystem.renderer().getImGui();
        if (imGui.getWindow() == window) {
            imGui.getImplGlfw().cursorEnterCallback(window, entered);
        }
    }
}
