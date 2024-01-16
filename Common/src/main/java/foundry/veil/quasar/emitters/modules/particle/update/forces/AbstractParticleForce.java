package foundry.veil.quasar.emitters.modules.particle.update.forces;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateModule;

public abstract class AbstractParticleForce implements UpdateModule {
    public float strength;
    public float falloff;

    public abstract void applyForce(QuasarParticle particle);

    @Override
    public void run(QuasarParticle particle) {
        applyForce(particle);
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public float getFalloff() {
        return falloff;
    }

    public void setFalloff(float falloff) {
        this.falloff = falloff;
    }

    public boolean shouldRemove() {
        return false;
    }

    public abstract <T extends AbstractParticleForce> T copy();
}
