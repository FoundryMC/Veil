package foundry.veil.mixin.client.deferred;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.wrapper.VeilRenderBridge;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    private Frustum cullingFrustum;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getShader()Lnet/minecraft/client/renderer/ShaderInstance;", shift = At.Shift.AFTER))
    public void updateUniforms(RenderType $$0, PoseStack $$1, double $$2, double $$3, double $$4, Matrix4f $$5, CallbackInfo ci) {
        ShaderInstance shader = RenderSystem.getShader();
        Uniform iModelViewMat = shader.getUniform("NormalMat");
        if (iModelViewMat != null) {
            iModelViewMat.set($$1.last().pose().normal(new Matrix3f()));
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getModelViewStack()Lcom/mojang/blaze3d/vertex/PoseStack;", shift = At.Shift.AFTER))
    public void endTranslucent(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
        bufferSource.endBatch(RenderType.translucentMovingBlock());
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V", shift = At.Shift.BEFORE))
    public void setupOpaque(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().beginOpaque();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;bindWrite(Z)V", shift = At.Shift.AFTER))
    public void bindWrite(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().setup();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V", shift = At.Shift.BEFORE))
    public void endOpaque(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().end();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;long2ObjectEntrySet()Lit/unimi/dsi/fastutil/objects/ObjectSet;", shift = At.Shift.BEFORE))
    public void beginTranslucent(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().beginTranslucent();
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    public void blit(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().blit(VeilRenderBridge.create(this.cullingFrustum));
    }
}
