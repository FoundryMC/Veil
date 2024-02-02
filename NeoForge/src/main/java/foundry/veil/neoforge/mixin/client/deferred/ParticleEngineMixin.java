package foundry.veil.neoforge.mixin.client.deferred;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.impl.client.render.deferred.DeferredShaderStateCache;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Unique
    private final Object2ObjectArrayMap<ParticleRenderType, DeferredShaderStateCache> veil$cache = new Object2ObjectArrayMap<>();

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V", at = @At("HEAD"), remap = false)
    public void setupRenderState(PoseStack p_107337_, MultiBufferSource.BufferSource p_107338_, LightTexture p_107339_, Camera p_107340_, float p_107341_, Frustum clippingHelper, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().setup();
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V", at = @At("RETURN"), remap = false)
    public void clearRenderState(PoseStack p_107337_, MultiBufferSource.BufferSource p_107338_, LightTexture p_107339_, Camera p_107340_, float p_107341_, Frustum clippingHelper, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().clear();
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleRenderType;begin(Lcom/mojang/blaze3d/vertex/BufferBuilder;Lnet/minecraft/client/renderer/texture/TextureManager;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
    public void setShader(PoseStack p_107337_, MultiBufferSource.BufferSource p_107338_, LightTexture p_107339_, Camera p_107340_, float p_107341_, Frustum clippingHelper, CallbackInfo ci, PoseStack posestack, Iterator<ParticleRenderType> var8, ParticleRenderType type) {
        this.veil$cache.computeIfAbsent(type, unused -> new DeferredShaderStateCache()).setupRenderState(RenderSystem.getShader());
    }

    @Inject(method = "clearParticles", at = @At("TAIL"))
    public void clearParticles(CallbackInfo ci) {
        this.veil$cache.clear();
    }
}
