package foundry.veil.mixin.client.deferred;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.ext.LevelRendererExtension;
import foundry.veil.render.VeilVanillaShaders;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.wrapper.CullFrustum;
import foundry.veil.render.wrapper.DeferredShaderStateCache;
import foundry.veil.render.wrapper.VeilRenderBridge;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin implements LevelRendererExtension {

    @Shadow
    private Frustum cullingFrustum;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    @Nullable
    private Frustum capturedFrustum;

    @Unique
    private final DeferredShaderStateCache veil$cloudCache = new DeferredShaderStateCache();
    private final DeferredShaderStateCache veil$weatherCache = new DeferredShaderStateCache();
    private final DeferredShaderStateCache veil$worldborderCache = new DeferredShaderStateCache();

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

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", shift = At.Shift.BEFORE))
    public void preRain(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().setup();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderWorldBorder(Lnet/minecraft/client/Camera;)V", shift = At.Shift.AFTER))
    public void postRain(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().clear();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V", shift = At.Shift.BEFORE))
    public void preClouds(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().setup();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V", shift = At.Shift.AFTER))
    public void postClouds(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().clear();
    }

    // Add custom cloud shader
    @ModifyArg(method = "renderClouds", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V"))
    public Supplier<ShaderInstance> setCloudShader(Supplier<ShaderInstance> supplier) {
        return () -> this.veil$cloudCache.getShader(VeilVanillaShaders.getCloud());
    }

    // Add custom world weather
    @ModifyArg(method = "renderSnowAndRain", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V"))
    public Supplier<ShaderInstance> setWeatherShader(Supplier<ShaderInstance> supplier) {
        return () -> this.veil$weatherCache.getShader(supplier.get());
    }

    // Add custom world border shader
    @ModifyArg(method = "renderWorldBorder", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V"))
    public Supplier<ShaderInstance> setWorldBorderShader(Supplier<ShaderInstance> supplier) {
        return () -> this.veil$worldborderCache.getShader(VeilVanillaShaders.getWorldborder());
    }

    // This sets the blend function for rain correctly
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", shift = At.Shift.BEFORE))
    public void setRainBlend(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V", shift = At.Shift.BEFORE))
    public void endOpaque(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().end();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;long2ObjectEntrySet()Lit/unimi/dsi/fastutil/objects/ObjectSet;", shift = At.Shift.BEFORE))
    public void beginTranslucent(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().beginTranslucent();
    }

    // This makes sure the breaking texture is drawn into the opaque buffer
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V", ordinal = 0, shift = At.Shift.BEFORE))
    public void preDrawCrumblingOpaque(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().beginOpaque();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V", ordinal = 0, shift = At.Shift.AFTER))
    public void postDrawCrumblingOpaque(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().beginTranslucent();
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    public void blit(PoseStack $$0, float $$1, long $$2, boolean $$3, Camera $$4, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().blit();
    }

    @Override
    public CullFrustum veil$getCullFrustum() {
        return VeilRenderBridge.create(this.capturedFrustum != null ? this.capturedFrustum : this.cullingFrustum);
    }
}
