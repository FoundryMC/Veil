package foundry.veil.mixin.client.pipeline;

import foundry.veil.render.pipeline.VeilRenderSystem;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "runTick", at=@At("HEAD"))
    public void beginFrame(boolean $$0, CallbackInfo ci){
        VeilRenderSystem.beginFrame();
    }
}
