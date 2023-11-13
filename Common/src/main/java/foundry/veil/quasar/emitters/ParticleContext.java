package foundry.veil.quasar.emitters;

import foundry.veil.quasar.client.particle.QuasarParticle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ParticleContext {
    public Vec3 position;
    public Vec3 velocity;
    public Entity entity;
    public QuasarParticle particle;

    public ParticleContext(Vec3 position, Vec3 velocity){
        this.position = position;
        this.velocity = velocity;
    }

    public ParticleContext(Vec3 position, Vec3 velocity, Entity entity){
        this.position = position;
        this.velocity = velocity;
        this.entity = entity;
    }

    public ParticleContext(Vec3 position, Vec3 velocity, QuasarParticle particle){
        this.position = position;
        this.velocity = velocity;
        this.particle = particle;
    }

    public Vec3 getPosition() {
        if(this.entity != null) {
            return this.entity.position();
        } else if (this.particle != null) {
            return ((ParticleAccessorExtension)this.particle).getPosition();
        } else {
            return this.position;
        }
    }

    public Vec3 getVelocity() {
        if(this.entity != null) {
            return this.entity.getDeltaMovement();
        } else if (this.particle != null) {
            return ((ParticleAccessorExtension)this.particle).getVelocity();
        } else {
            return this.velocity;
        }
    }
}
