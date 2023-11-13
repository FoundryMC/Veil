package foundry.veil.quasar.emitters.modules.particle.update.forces;

import foundry.veil.quasar.QuasarClient;
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
 * A point force is used to apply a force in the direction away from a point.
 */
public class PointForce extends AbstractParticleForce{
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
        return point;
    }

    public void setPoint(Supplier<Vec3> point) {
        this.point = point;
    }

    public void setPoint(Vec3 point) {
        this.point = () -> point;
    }
    private float range;

    public float getRange() {
        return range;
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
    public void applyForce(QuasarParticle particle) {
        if(point == null) return;
        double dist = particle.getPos().subtract(point.get()).length();
        if(dist < range) {
            // apply force to particle to move away from the point
            Vec3 particleToPoint = point.get().subtract(particle.getPos());
            Vec3 particleToPointUnit = particleToPoint.normalize();
            Vec3 particleToPointUnitScaled = particleToPointUnit.scale(-strength);
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
        return !shouldStay.get();
    }

    @Override
    public void renderImGuiSettings() {
        if(ImGui.collapsingHeader("Point Force Settings #"+this.hashCode(), shouldStay)){
            ImGui.text("Point Force Settings");
            float[] strength = new float[]{this.strength};
            ImGui.text("Strength");
            ImGui.sameLine();
            ImGui.dragFloat("##Strength " + this.hashCode(), strength, 0.01f);
            this.strength = strength[0];
            ImFloat range = new ImFloat(this.getRange());
            ImGui.text("Range");
            ImGui.sameLine();
            ImGui.inputFloat("##Range #" +this.hashCode(), range);
            this.setRange(range.get());
            float[] pos = new float[]{(float) this.getPoint().get().x, (float) this.getPoint().get().y, (float) this.getPoint().get().z};
            ImGui.text("Position:");
            ImGui.sameLine();
            ImGui.dragFloat3("##Position: #" +this.hashCode(), pos);
            this.setPoint(new Vec3(pos[0], pos[1], pos[2]));
            if(ImGui.button("Set force center to emitter pos")) {
                this.setPoint(QuasarClient.editorScreen.currentlySelectedEmitterInstance.getEmitterModule().getPosition());
            }
        }
    }

    @Override
    public PointForce copy() {
        return new PointForce(point, range, strength, falloff);
    }

    
}
