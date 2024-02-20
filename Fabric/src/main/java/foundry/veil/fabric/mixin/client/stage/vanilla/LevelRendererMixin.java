package foundry.veil.fabric.mixin.client.stage.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.fabric.ext.LevelRendererExtension;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;clearRenderState()V", shift = At.Shift.BEFORE))
    public void postRenderChunkLayer(RenderType renderType, PoseStack poseStack, double d, double e, double f, Matrix4f projection, CallbackInfo ci) {
        ((LevelRendererExtension) this).veil$renderStage(renderType, poseStack, projection);
    }
}
