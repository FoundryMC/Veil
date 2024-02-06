package foundry.veil.mixin.client.quasar;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.quasar.ParticleSystemManager;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Inject(method = "countParticles", at = @At("RETURN"), cancellable = true)
    public void countParticles(CallbackInfoReturnable<String> cir) {
        ParticleSystemManager particleManager = VeilRenderSystem.renderer().getParticleManager();
        cir.setReturnValue(cir.getReturnValue() + ". VE: " + particleManager.getEmitterCount() + ". VP: " + particleManager.getParticleCount());
    }
}
