package foundry.veil.mixin.debug;

import foundry.veil.Veil;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin {

    @Inject(method = "main", at = @At("HEAD"), remap = false)
    private static void main(String[] pArgs, CallbackInfo ci) {
        if (Veil.RENDER_DOC) {
            System.loadLibrary("renderdoc");
        }
    }
}
