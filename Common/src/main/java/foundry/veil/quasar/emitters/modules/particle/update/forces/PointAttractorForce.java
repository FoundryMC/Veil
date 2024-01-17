package foundry.veil.quasar.emitters.modules.particle.update.forces;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A force that attracts particles to a point.
 * @see AbstractParticleForce
 * @see foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule
 * <p>
 *     Point attractor forces are forces that are applied in the direction of a point.
 *     They are useful for simulating gravity or other forces that pull particles towards a point.
 *     The strength of the force is determined by the strength parameter.
 *     The falloff parameter determines how quickly the force falls off with distance. (unused)
 *     The strengthByDistance parameter determines whether the strength of the force is multiplied by the distance from the point.
 *     If strengthByDistance is true, the strength of the force is multiplied by (1 - distance / range).
 *     If strengthByDistance is false, the strength of the force is not affected by distance.
 *     The range parameter determines the maximum distance from the point at which the force is applied.
 *     If the distance from the point is greater than the range, the force is not applied.
 *     The position parameter determines the position of the point.
 *     The position parameter can be a Vec3 or a Supplier Vec3.
 *     If the position parameter is a Vec3, the position of the point is fixed.
 *     If the position parameter is a Supplier Vec3, the position of the point is updated every tick.
 *     This allows the point to move.
 * </p>
 */
public class PointAttractorForce extends AbstractParticleForce {
    public static final Codec<PointAttractorForce> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Vec3.CODEC.fieldOf("position").forGetter(p -> p.getPosition().get()),
                    Codec.FLOAT.fieldOf("range").forGetter(PointAttractorForce::getRange),
                    Codec.FLOAT.fieldOf("strength").forGetter(PointAttractorForce::getStrength),
                    Codec.FLOAT.fieldOf("falloff").forGetter(PointAttractorForce::getFalloff),
                    Codec.BOOL.fieldOf("strengthByDistance").forGetter(PointAttractorForce::isStrengthByDistance),
                    Codec.BOOL.fieldOf("invertDistanceModifier").orElse(false).forGetter(PointAttractorForce::isInvertDistanceModifier)
            ).apply(instance, PointAttractorForce::new)
            );
    Supplier<Vec3> position;
    public Supplier<Vec3> getPosition() {
        return position;
    }
    public void setPosition(Supplier<Vec3> position) {
        this.position = position;
    }
    public void setPosition(Vec3 position) {
        this.position = () -> position;
    }
    float range;
    public float getRange() {
        return range;
    }
    boolean strengthByDistance;
    public boolean isStrengthByDistance() {
        return strengthByDistance;
    }
    boolean invertDistanceModifier = false;
    public boolean isInvertDistanceModifier() {
        return invertDistanceModifier;
    }

    public PointAttractorForce(Vec3 position, float range, float strength, float decay, boolean strengthByDistance, boolean invertDistanceModifier) {
        this.position = () -> position;
        this.range = range;
        this.strength = strength;
        this.falloff = decay;
        this.strengthByDistance = strengthByDistance;
        this.invertDistanceModifier = invertDistanceModifier;
    }

    public PointAttractorForce(Supplier<Vec3> position, float range, float strength, float decay, boolean strengthByDistance, boolean invertDistanceModifier) {
        this.position = position;
        this.range = range;
        this.strength = strength;
        this.falloff = decay;
        this.strengthByDistance = strengthByDistance;
        this.invertDistanceModifier = invertDistanceModifier;
    }
    @Override
    public void applyForce(QuasarParticle particle) {
        Vec3 particlePos = particle.getPos();
        Vec3 diff = particlePos.subtract(position.get());
        float distance = (float)diff.length();
        if(distance < range) {
            float strength = this.strength;
            if(strengthByDistance && !invertDistanceModifier) {
                strength = strength * (1 - distance / range);
            } else if(strengthByDistance && invertDistanceModifier) {
                strength = strength * (distance / range) * 2;
            }
            particle.addForce(diff.normalize().scale(-strength));
        }
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.POINT_ATTRACTOR;
    }

    public ImBoolean shouldStay = new ImBoolean(true);

    @Override
    public boolean shouldRemove() {
        return !shouldStay.get();
    }

    @Override
    public PointAttractorForce copy() {
        return new PointAttractorForce(position, range, strength, falloff, strengthByDistance, invertDistanceModifier);
    }

    public void setRange(float range) {
        this.range = range;
    }
}
