package foundry.veil.mixin.client;

import foundry.veil.postprocessing.PostProcessingHandler;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "resize", at = @At(value = "HEAD"))
    public void resizeListener(int pWidth, int pHeight, CallbackInfo ci){
        PostProcessingHandler.resize(pWidth, pHeight);
    }
}