package foundry.veil.quasar.emitters.modules.particle.update.forces;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;

public abstract class AbstractParticleForce implements UpdateParticleModule {

    protected float strength;
    protected float falloff;

    public abstract void applyForce(QuasarVanillaParticle particle);

    @Override
    public void run(QuasarVanillaParticle particle) {
        this.applyForce(particle);
    }

    public float getStrength() {
        return this.strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public float getFalloff() {
        return this.falloff;
    }

    public void setFalloff(float falloff) {
        this.falloff = falloff;
    }

    public boolean shouldRemove() {
        return false;
    }

    public abstract AbstractParticleForce copy();
}


