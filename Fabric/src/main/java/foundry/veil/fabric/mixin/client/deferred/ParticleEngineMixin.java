package foundry.veil.fabric.mixin.client.deferred;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.impl.client.render.deferred.DeferredShaderStateCache;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Unique
    private final DeferredShaderStateCache veil$cache = new DeferredShaderStateCache();

    @Inject(method = "render", at = @At("HEAD"))
    public void setupRenderState(PoseStack $$0, MultiBufferSource.BufferSource $$1, LightTexture $$2, Camera $$3, float $$4, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().setup();
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void clearRenderState(PoseStack $$0, MultiBufferSource.BufferSource $$1, LightTexture $$2, Camera $$3, float $$4, CallbackInfo ci) {
        VeilRenderSystem.renderer().getDeferredRenderer().clear();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V"), index = 0, remap = false)
    public Supplier<ShaderInstance> setShader(Supplier<ShaderInstance> value) {
        return () -> this.veil$cache.getShader(value.get());
    }
}
