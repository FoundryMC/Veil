package foundry.veil.mixin;

import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import foundry.veil.quasar.util.EntityExtension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtension {
    @Shadow public abstract boolean isOnFire();

    @Unique
    List<ParticleEmitter> emitters = new ArrayList<>();

    @Override
    public List<ParticleEmitter> getEmitters() {
        return emitters;
    }

    @Override
    public void addEmitter(ParticleEmitter emitter) {
        emitters.add(emitter);
    }

    @Inject(method = "onClientRemoval", at = @At("TAIL"))
    public void remove(CallbackInfo ci) {
        if(!((Entity) (Object) this).level().isClientSide) return;
        emitters.forEach(ParticleSystemManager.getInstance()::removeDelayedParticleSystem);
        emitters.clear();
    }

    @Override
    public Vec3 getHitboxCenterPos() {
        return ((Entity) (Object) this).position().add(0, ((Entity) (Object) this).getBbHeight()/2f, 0);
    }
}
