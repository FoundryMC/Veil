package foundry.veil.quasar.data.module.force;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;
import foundry.veil.quasar.emitters.modules.particle.update.forces.AbstractParticleForce;
import imgui.type.ImBoolean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A force that applies a vortex force to a particle.
 * @see AbstractParticleForce
 * @see UpdateParticleModule
 * <p>
 *     Vortex forces are forces that are applied in a circular motion around a center point.
 *     They are useful for simulating whirlpools or tornadoes.
 *     The strength of the force is determined by the strength parameter.
 *     The falloff parameter determines how quickly the force falls off with distance. (unused)
 */
public class VortexForce extends AbstractParticleForce {
    public static final Codec<VortexForce> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Vec3.CODEC.fieldOf("vortex_axis").forGetter(VortexForce::getVortexAxis),
                    Vec3.CODEC.fieldOf("vortex_center").forGetter(VortexForce::getVortexCenter),
                    Codec.FLOAT.fieldOf("range").forGetter(VortexForce::getRange),
                    Codec.FLOAT.fieldOf("strength").forGetter(VortexForce::getStrength),
                    Codec.FLOAT.fieldOf("falloff").forGetter(VortexForce::getFalloff)
            ).apply(instance, VortexForce::new)
            );
    private Vec3 vortexAxis;
    public Vec3 getVortexAxis() {
        return vortexAxis;
    }
    public void setVortexAxis(Vec3 vortexAxis) {
        this.vortexAxis = vortexAxis;
    }
    private Supplier<Vec3> vortexCenter;
    public Vec3 getVortexCenter() {
        return vortexCenter.get();
    }
    public void setVortexCenter(Supplier<Vec3> vortexCenter) {
        this.vortexCenter = vortexCenter;
    }
    public void setVortexCenter(Vec3 vortexCenter) {
        this.vortexCenter = () -> vortexCenter;
    }
    private float range;
    public float getRange() {
        return range;
    }
    public void setRange(float range) {
        this.range = range;
    }

    public VortexForce(Vec3 vortexAxis, Vec3 vortexCenter, float range, float strength, float decay) {
        this.vortexAxis = vortexAxis;
        this.vortexCenter = () -> vortexCenter;
        this.strength = strength;
        this.falloff = decay;
        this.range = range;
    }

    public VortexForce(Vec3 vortexAxis, Supplier<Vec3> vortexCenter, float range, float strength, float decay) {
        this.vortexAxis = vortexAxis;
        this.vortexCenter = vortexCenter;
        this.strength = strength;
        this.falloff = decay;
        this.range = range;
    }
    @Override
    public void applyForce(QuasarVanillaParticle particle) {
        double dist = particle.getPos().subtract(vortexCenter.get()).length();
        if(dist < range) {
            // apply force to particle to move around the vortex center on the vortex axis, but do not modify outwards/inwards velocity
            Vec3 particleToCenter = vortexCenter.get().subtract(particle.getPos());
            Vec3 particleToCenterOnAxis = particleToCenter.subtract(vortexAxis.scale(particleToCenter.dot(vortexAxis)));
            Vec3 particleToCenterOnAxisUnit = particleToCenterOnAxis.normalize();
            Vec3 particleToCenterOnAxisUnitCrossVortexAxis = particleToCenterOnAxisUnit.cross(vortexAxis);
            Vec3 particleToCenterOnAxisUnitCrossVortexAxisUnit = particleToCenterOnAxisUnitCrossVortexAxis.normalize();
            Vec3 particleToCenterOnAxisUnitCrossVortexAxisUnitScaled = particleToCenterOnAxisUnitCrossVortexAxisUnit.scale(strength);
            particle.addForce(particleToCenterOnAxisUnitCrossVortexAxisUnitScaled);
        }
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.VORTEX;
    }

    public ImBoolean shouldStay = new ImBoolean(true);

    @Override
    public boolean shouldRemove() {
        return !shouldStay.get();
    }

    @Override
    public VortexForce copy() {
        return new VortexForce(vortexAxis, vortexCenter, range, strength, falloff);
    }

    
}
