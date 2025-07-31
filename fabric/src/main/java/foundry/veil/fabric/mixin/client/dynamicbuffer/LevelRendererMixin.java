package foundry.veil.fabric.mixin.client.dynamicbuffer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.impl.client.render.dynamicbuffer.DynamicBufferShard;
import net.minecraft.client.renderer.LevelRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 200) // Apply these changes last
public abstract class LevelRendererMixin {

    @Shadow
    @Nullable
    public abstract RenderTarget getParticlesTarget();

    @Unique
    private final DynamicBufferShard veil$particleBufferShard = new DynamicBufferShard("particles", this::getParticlesTarget);

    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=destroyProgress"))
    public void beginTranslucent(CallbackInfo ci) {
        VeilRenderSystem.renderer().getDynamicBufferManger().setEnabled(true);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V", shift = At.Shift.BEFORE))
    public void preRenderParticles(CallbackInfo ci) {
        this.veil$particleBufferShard.setupRenderState();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V", shift = At.Shift.AFTER))
    public void postRenderParticles(CallbackInfo ci) {
        this.veil$particleBufferShard.clearRenderState();
    }
}
