package foundry.veil.mixin.client;

import foundry.veil.quasar.emitters.ParticleAccessorExtension;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Particle.class)
public class ParticleMixin implements ParticleAccessorExtension {

    @Override
    public Vec3 getPosition(){
        return new Vec3(((ParticleAccessor)this).getX(), ((ParticleAccessor) this).getY(), ((ParticleAccessor)this).getZ());
    }

    @Override
    public Vec3 getVelocity(){
        return new Vec3(((ParticleAccessor)this).getXd(), ((ParticleAccessor)this).getYd(), ((ParticleAccessor)this).getZd());
    }

    @Override
    public ClientLevel getClientLevel(){
        return ((ParticleAccessor)this).getLevel();
    }
}
