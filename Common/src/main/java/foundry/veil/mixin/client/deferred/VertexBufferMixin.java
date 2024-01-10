package foundry.veil.mixin.client.deferred;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexBuffer.class)
public class VertexBufferMixin {

    @Inject(method = "_drawWithShader", at = @At("HEAD"))
    public void _drawWithShader(Matrix4f modelView, Matrix4f projection, ShaderInstance shader, CallbackInfo ci) {
        Uniform iModelViewMat = shader.getUniform("NormalMat");
        if (iModelViewMat != null) {
            iModelViewMat.set(modelView.normal(new Matrix3f()));
        }
    }
}
