package foundry.veil.mixin.debug;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;

@Mixin(KeyboardHandler.class)
public abstract class DebugKeyboardHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract boolean handleChunkDebugKeys(int key);

    @Inject(method = "handleDebugKeys", at = @At("RETURN"), cancellable = true)
    public void handleChunkDebugKeys(int key, CallbackInfoReturnable<Boolean> cir) {
        if (this.handleChunkDebugKeys(key)) {
            cir.setReturnValue(true);
            return;
        }

        if (key == GLFW_KEY_Q) {
            ChatComponent $$3 = this.minecraft.gui.getChat();
            $$3.addMessage(Component.literal("F3 + E = Show VisGraph Path"));
            $$3.addMessage(Component.literal("F3 + L = Disable Smart Cull"));
            $$3.addMessage(Component.literal("F3 + U = Capture View Frustum"));
            $$3.addMessage(Component.literal("F3 + SHIFT + U = Kill Captured View Frustum"));
            $$3.addMessage(Component.literal("F3 + V = Show VisGraph Visibility"));
            $$3.addMessage(Component.literal("F3 + W = Show Wireframe"));
        }
    }
}
