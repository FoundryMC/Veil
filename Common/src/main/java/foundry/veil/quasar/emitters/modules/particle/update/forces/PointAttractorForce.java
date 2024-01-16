package foundry.veil.quasar.emitters.modules.particle.update.forces;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;
import imgui.type.ImBoolean;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * A force that attracts particles to a point.
 *
 * <p>
 * Point attractor forces are forces that are applied in the direction of a point.
 * They are useful for simulating gravity or other forces that pull particles towards a point.
 * The strength of the force is determined by the strength parameter.
 * The falloff parameter determines how quickly the force falls off with distance. (unused)
 * The strengthByDistance parameter determines whether the strength of the force is multiplied by the distance from the point.
 * If strengthByDistance is true, the strength of the force is multiplied by (1 - distance / range).
 * If strengthByDistance is false, the strength of the force is not affected by distance.
 * The range parameter determines the maximum distance from the point at which the force is applied.
 * If the distance from the point is greater than the range, the force is not applied.
 * The position parameter determines the position of the point.
 * The position parameter can be a Vec3 or a Supplier<Vec3>.
 * If the position parameter is a Vec3, the position of the point is fixed.
 * If the position parameter is a Supplier<Vec3>, the position of the point is updated every tick.
 * This allows the point to move.
 * </p>
 *
 * @see AbstractParticleForce
 * @see UpdateParticleModule
 * @author amo
 */
public class PointAttractorForce extends AbstractParticleForce {
    public static final Codec<PointAttractorForce> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.fieldOf("position").forGetter(p -> p.getPosition().get()),
            Codec.FLOAT.fieldOf("range").forGetter(PointAttractorForce::getRange),
            Codec.FLOAT.fieldOf("strength").forGetter(PointAttractorForce::getStrength),
            Codec.FLOAT.fieldOf("falloff").forGetter(PointAttractorForce::getFalloff),
            Codec.BOOL.fieldOf("strengthByDistance").forGetter(PointAttractorForce::isStrengthByDistance),
            Codec.BOOL.fieldOf("invertDistanceModifier").orElse(false).forGetter(PointAttractorForce::isInvertDistanceModifier)
    ).apply(instance, PointAttractorForce::new));

    private final Vector3d position;
    private final float range;
    private final boolean strengthByDistance;
    private final boolean invertDistanceModifier;
    public ImBoolean shouldStay = new ImBoolean(true);

    public PointAttractorForce(Vector3dc position, float range, float strength, float decay, boolean strengthByDistance, boolean invertDistanceModifier) {
        this.position = new Vector3d(position);
        this.range = range;
        this.strength = strength;
        this.falloff = decay;
        this.strengthByDistance = strengthByDistance;
        this.invertDistanceModifier = invertDistanceModifier;
    }

    @Override
    public void applyForce(QuasarParticle particle) {
        Vec3 particlePos = particle.getPos();
        Vec3 diff = particlePos.subtract(this.position.get());
        float distance = (float) diff.length();
        if (distance < this.range) {
            float strength = this.strength;

            if (this.strengthByDistance) {
                if (!this.invertDistanceModifier) {
                    strength = strength * (1 - distance / this.range);
                } else {
                    strength = strength * (distance / this.range) * 2;
                }
            }
            particle.addForce(diff.normalize().scale(-strength));
        }
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.POINT_ATTRACTOR;
    }

    @Override
    public boolean shouldRemove() {
        return !this.shouldStay.get();
    }

    @Override
    public PointAttractorForce copy() {
        return new PointAttractorForce(this.position, this.range, this.strength, this.falloff, this.strengthByDistance, this.invertDistanceModifier);
    }

    public Vector3dc getPosition() {
        return this.position;
    }

    public void setPosition(Vector3dc position) {
        this.position.set(position);
    }

    public void setPosition(double x, double y, double z) {
        this.position.set(x, y, z);
    }

    public float getRange() {
        return this.range;
    }

    public boolean isStrengthByDistance() {
        return this.strengthByDistance;
    }

    public boolean isInvertDistanceModifier() {
        return this.invertDistanceModifier;
    }
}
