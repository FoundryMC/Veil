package foundry.veil.quasar.emitters.modules.particle.update.forces;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import imgui.type.ImBoolean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A point force is used to apply a force in the direction away from a point.
 */
public class PointForce extends AbstractParticleForce {
    public static final Codec<PointForce> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Vec3.CODEC.fieldOf("point").orElse(new Vec3(69,69,96)).forGetter(p -> p.getPoint().get()),
                    Codec.FLOAT.fieldOf("range").forGetter(PointForce::getRange),
                    Codec.FLOAT.fieldOf("strength").forGetter(PointForce::getStrength),
                    Codec.FLOAT.fieldOf("falloff").forGetter(PointForce::getFalloff)
            ).apply(instance, PointForce::new)
            );
    private Supplier<Vec3> point;
    public Supplier<Vec3> getPoint() {
        return this.point;
    }

    public void setPoint(Supplier<Vec3> point) {
        this.point = point;
    }

    public void setPoint(Vec3 point) {
        this.point = () -> point;
    }
    private float range;

    public float getRange() {
        return this.range;
    }

    public void setRange(float range) {
        this.range = range;
    }
    public PointForce(Vec3 point, float range, float strength, float decay) {
        this.point = () -> point;
        this.strength = strength;
        this.falloff = decay;
        this.range = range;
    }

    public PointForce(Supplier<Vec3> point, float range, float strength, float decay) {
        this.point = point;
        this.strength = strength;
        this.falloff = decay;
        this.range = range;
    }


    @Override
    public void applyForce(QuasarVanillaParticle particle) {
        if(this.point == null) return;
        double dist = particle.getPos().subtract(this.point.get()).length();
        if(dist < this.range) {
            // apply force to particle to move away from the point
            Vec3 particleToPoint = this.point.get().subtract(particle.getPos());
            Vec3 particleToPointUnit = particleToPoint.normalize();
            Vec3 particleToPointUnitScaled = particleToPointUnit.scale(-this.strength);
            particle.addForce(particleToPointUnitScaled);
        }
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.POINT;
    }
    public ImBoolean shouldStay = new ImBoolean(true);

    @Override
    public boolean shouldRemove() {
        return !this.shouldStay.get();
    }

    @Override
    public PointForce copy() {
        return new PointForce(this.point, this.range, this.strength, this.falloff);
    }

    
}
