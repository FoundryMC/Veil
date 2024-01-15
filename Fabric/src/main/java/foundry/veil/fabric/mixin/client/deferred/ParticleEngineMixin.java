package foundry.veil.fabric.mixin.client.deferred;

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

    @Inject(method = "render", at = @At("HEAD"))
    public void setupRenderState(PoseStack $$0, MultiBufferSource.BufferSource $$1, LightTexture $$2, Camera $$3, float $$4, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().setup();
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void clearRenderState(PoseStack $$0, MultiBufferSource.BufferSource $$1, LightTexture $$2, Camera $$3, float $$4, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().clear();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleRenderType;begin(Lcom/mojang/blaze3d/vertex/BufferBuilder;Lnet/minecraft/client/renderer/texture/TextureManager;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void setShader(PoseStack $$0, MultiBufferSource.BufferSource $$1, LightTexture $$2, Camera $$3, float $$4, CallbackInfo ci, PoseStack modelViewStack, Iterator<ParticleRenderType> iterator, ParticleRenderType type) {
        this.veil$cache.computeIfAbsent(type, unused -> new DeferredShaderStateCache()).setupRenderState(RenderSystem.getShader());
    }

    @Inject(method = "clearParticles", at = @At("TAIL"))
    public void clearParticles(CallbackInfo ci) {
        this.veil$cache.clear();
    }
}
