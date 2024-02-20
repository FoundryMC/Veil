package foundry.veil.fabric.mixin.client.quasar;

import foundry.veil.fabric.FabricQuasarParticleHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "setLevel", at = @At("HEAD"))
    public void setLevel(ClientLevel level, CallbackInfo ci) {
        FabricQuasarParticleHandler.setLevel(level);
    }
}
