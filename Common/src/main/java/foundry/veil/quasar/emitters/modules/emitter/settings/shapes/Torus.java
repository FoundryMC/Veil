package foundry.veil.quasar.emitters.modules.emitter.settings.shapes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class Torus extends AbstractEmitterShape {
    @Override
    public Vec3 getPoint(RandomSource randomSource, Vec3 dimensions, Vec3 rotation, Vec3 position, boolean fromSurface) {
        double theta = randomSource.nextDouble() * 2 * Math.PI;
        double phi = randomSource.nextDouble() * 2 * Math.PI;
        double x = Math.cos(theta) * (1 + 0.5 * Math.cos(phi));
        double y = Math.sin(theta) * (1 + 0.5 * Math.cos(phi));
        double z = dimensions.z() * Math.sin(phi);
        Vec3 normal = new Vec3(x, y, z).normalize();
        if(!fromSurface){
            normal = normal.scale(randomSource.nextDouble()).normalize();
            dimensions = dimensions.multiply(
                    randomSource.nextDouble(),
                    randomSource.nextDouble(),
                    randomSource.nextDouble()
            );
        }
        Vec3 pos = normal.multiply(dimensions);
        pos = pos.xRot((float) Math.toRadians(rotation.x())).yRot((float) Math.toRadians(rotation.y())).zRot((float) Math.toRadians(rotation.z()));
        return pos.add(position);
    }

    @Override
    public void renderShape(PoseStack stack, VertexConsumer consumer, Vec3 dimensions, Vec3 rotation) {
        float radius = (float) dimensions.x();
        float tubeRadius = (float) dimensions.y();
        float angle = 0;
        float angleStep = 360f / 32f;
        float x = (float) 0;
        float y = (float) 0;
        float z = (float) 0;
        for(int i = 0; i < 32; i++){
            float x1 = (float) (x + Math.cos(Math.toRadians(angle)) * radius);
            float z1 = (float) (z + Math.sin(Math.toRadians(angle)) * radius);
            float x2 = (float) (x + Math.cos(Math.toRadians(angle + angleStep)) * radius);
            float z2 = (float) (z + Math.sin(Math.toRadians(angle + angleStep)) * radius);
            consumer.vertex(stack.last().pose(), x1, y, z1).color(0.15f, 0.15f, 1, 1).normal(0, 1, 0).endVertex();
            consumer.vertex(stack.last().pose(), x2, y, z2).color(0.15f, 0.15f, 1, 1).normal(0, 1, 0).endVertex();
            angle += angleStep;
        }
    }
}
