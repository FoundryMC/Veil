package foundry.veil.mixin.client.deferred;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getShader()Lnet/minecraft/client/renderer/ShaderInstance;", shift = At.Shift.AFTER))
    public void updateUniforms(RenderType $$0, PoseStack $$1, double $$2, double $$3, double $$4, Matrix4f $$5, CallbackInfo ci) {
        ShaderInstance shader = RenderSystem.getShader();
        Uniform iModelViewMat = shader.getUniform("NormalMat");
        if (iModelViewMat != null) {
            iModelViewMat.set($$1.last().pose().normal(new Matrix3f()));
        }
    }
}
