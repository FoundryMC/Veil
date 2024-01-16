package foundry.veil.quasar.emitters.modules.particle.update.forces;

import com.mojang.serialization.Codec;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;
import imgui.type.ImBoolean;

/**
 * A force that applies a gravity force to a particle.
 *
 * @see AbstractParticleForce
 * @see UpdateParticleModule
 */
public class GravityForce extends AbstractParticleForce {

    public static final Codec<GravityForce> CODEC = Codec.FLOAT.fieldOf("strength").xmap(GravityForce::new, GravityForce::getStrength).codec();

    public ImBoolean shouldStay = new ImBoolean(true);

    public GravityForce(float strength) {
        this.strength = strength;
    }

    @Override
    public void applyForce(QuasarParticle particle) {
        particle.setGravity(this.strength);
    }

    @Override
    public GravityForce copy() {
        return new GravityForce(this.strength);
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.GRAVITY;
    }

    @Override
    public boolean shouldRemove() {
        return !this.shouldStay.get();
    }
}
