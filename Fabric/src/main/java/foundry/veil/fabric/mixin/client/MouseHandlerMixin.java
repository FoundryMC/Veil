package foundry.veil.fabric.mixin.client;

import foundry.veil.VeilClient;
import foundry.veil.render.pipeline.VeilRenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onPress", at = @At("HEAD"))
    public void keyPress(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window == this.minecraft.getWindow().getWindow() && action == GLFW_PRESS && VeilClient.EDITOR_KEY.matchesMouse(button)) {
            VeilRenderSystem.renderer().getEditorManager().toggle();
        }
    }
}
