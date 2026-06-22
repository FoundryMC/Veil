package foundry.veil.api.quasar.emitters.shape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.RandomSource;
import org.joml.*;

import java.lang.Math;

public class Cylinder implements EmitterShape {

    @Override
    public Vector3d getPoint(RandomSource randomSource, Vector3fc dimensions, Vector3fc rotation, Vector3dc position, boolean fromSurface) {
        double theta = randomSource.nextDouble() * 2 * Math.PI;
        double x = Math.cos(theta);
        double y = randomSource.nextDouble() * 2 - 1;
        double z = Math.sin(theta);
        Vector3d normal = new Vector3d(x, 0, z).normalize().add(0, y, 0);
        Vector3fc dim = dimensions;
        if (!fromSurface) {
            normal.mul(randomSource.nextDouble()).normalize();
            dim = dimensions.mul(
                    randomSource.nextFloat(),
                    randomSource.nextFloat(),
                    randomSource.nextFloat(),
                    new Vector3f()
            );
        }
        Vector3d pos = normal.mul(dim);
        pos = pos.rotate(new Quaterniond().rotationXYZ((float) Math.toRadians(rotation.x()), (float) Math.toRadians(rotation.y()), (float) Math.toRadians(rotation.z())));
        pos.mul(0.5);
        return pos.add(position);
    }

    @Override
    public void renderShape(PoseStack stack, VertexConsumer consumer, Vector3fc dimensions, Vector3fc rotation) {
        stack.pushPose();

        stack.mulPose(new Quaternionf().rotationXYZ((float) Math.toRadians(rotation.x()), (float) Math.toRadians(rotation.y()), (float) Math.toRadians(rotation.z())));
        stack.scale(dimensions.x(), dimensions.y(), dimensions.z());

        float radius = 0.5f;
        float angle = 0;
        float angleStep = 360f / 32f;
        float x = 0;
        float y;
        float z = 0;
        for (y = 0.5f; y >= -0.5f; y -= 1.0f) {
            for (int i = 0; i < 32; i++) {
                float x1 = (float) (x + Math.cos(Math.toRadians(angle)) * radius);
                float z1 = (float) (z + Math.sin(Math.toRadians(angle)) * radius);
                consumer.addVertex(stack.last().pose(), x1, y, z1).setColor(0.15f, 0.15f, 1, 1).setNormal(0, 1, 0);
                angle += angleStep;
            }
        }

        for (int i = 0; i < 32; i++) {
            float x1 = (float) (x + Math.cos(Math.toRadians(angle)) * radius);
            float z1 = (float) (z + Math.sin(Math.toRadians(angle)) * radius);
            consumer.addVertex(stack.last().pose(), x1,  0.5f, z1).setColor(0.15f, 0.15f, 1, 1).setNormal(0, 1, 0);
            consumer.addVertex(stack.last().pose(), x1, -0.5f, z1).setColor(0.15f, 0.15f, 1, 1).setNormal(0, 1, 0);
            angle += angleStep;
        }


        stack.popPose();
    }
}
