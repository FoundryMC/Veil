package foundry.veil.fabric.mixin.client;

import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At("TAIL"))
    public void keyPress(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        if (window == this.minecraft.getWindow().getWindow() && action == GLFW_PRESS && VeilClient.EDITOR_KEY.matches(key, scancode)) {
            VeilRenderSystem.renderer().getEditorManager().toggle();
        }
    }
}
