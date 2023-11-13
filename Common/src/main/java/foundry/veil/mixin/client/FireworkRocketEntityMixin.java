package foundry.veil.mixin.client;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin {
    @Inject(method = "handleEntityEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;createFireworks(DDDDDDLnet/minecraft/nbt/CompoundTag;)V"), cancellable = true)
    public void handleEntityEvent(byte pId, CallbackInfo ci) {
        FireworkRocketEntity self = (FireworkRocketEntity) (Object) this;
        Vec3 pos = self.position();
//        ParticleRegistry.runFireworkParticles(new ParticleContext(self.position(), self.getDeltaMovement(), self));
        self.level().playLocalSound(pos.x, pos.y, pos.z, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.AMBIENT, 20.0F, 0.95F + self.level().random.nextFloat() * 0.1F, true);
        ci.cancel();
    }
}
