package foundry.veil.quasar.client.particle.data;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;

public class QuasarParticleRenderData {
    public Vec3 motionDirection;
    public Vec3 lerpedPos;
    public int light;
    public VertexConsumer builder;
    public double ageModifier;
    public float partialTicks;

    public QuasarParticleRenderData(Vec3 motionDirection, Vec3 lerpedPos, int light, VertexConsumer builder, double ageModifier, float partialTicks) {
        this.motionDirection = motionDirection;
        this.lerpedPos = lerpedPos;
        this.light = light;
        this.builder = builder;
        this.ageModifier = ageModifier;
        this.partialTicks = partialTicks;
    }
}