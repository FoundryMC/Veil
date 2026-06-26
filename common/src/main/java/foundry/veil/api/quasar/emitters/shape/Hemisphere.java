package foundry.veil.api.quasar.emitters.shape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.RandomSource;
import org.joml.*;

import java.lang.Math;

import static foundry.veil.api.quasar.emitters.shape.Sphere.parametricSphere;

public class Hemisphere implements EmitterShape {

    @Override
    public Vector3d getPoint(RandomSource randomSource, Vector3fc dimensions, Vector3fc rotation, Vector3dc position, boolean fromSurface) {
        double theta = randomSource.nextDouble() * 2 * Math.PI;
        double phi = randomSource.nextDouble() * Math.PI / 2;
        double x = Math.cos(theta) * Math.sin(phi);
        double y = Math.sin(theta) * Math.sin(phi);
        double z = Math.cos(phi);
        Vector3d normal = new Vector3d(x, y, z).normalize();
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
        Vector3d pos = normal.mul(dim).mul(0.5);
        pos = pos.rotate(new Quaterniond().rotationXYZ((float) Math.toRadians(rotation.x()), (float) Math.toRadians(rotation.y()), (float) Math.toRadians(rotation.z())));
        return pos.add(position);
    }

    @Override
    public void renderShape(PoseStack stack, VertexConsumer consumer, Vector3fc dimensions, Vector3fc rotation) {
        stack.pushPose();

        stack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(rotation.x())).rotateY((float) Math.toRadians(rotation.y())).rotateZ((float) Math.toRadians(rotation.z())));
        stack.scale(dimensions.x(), dimensions.y(), dimensions.z());

        float radius = 0.5f;
        Matrix4f matrix4f = stack.last().pose();
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                Vector3f v1 = parametricSphere((float) Math.toRadians(i * 11.25f), (float) Math.toRadians(j * 11.25f), radius);
                if (v1.z < -0.01) continue;
                Vector3f v2 = parametricSphere((float) Math.toRadians((i + 1) * 11.25f), (float) Math.toRadians(j * 11.25f), radius);
                consumer.addVertex(matrix4f, v1.x(), v1.y(), v1.z()).setColor(0.15f, 0.15f, 1, 1).setNormal(0, 1, 0);
                consumer.addVertex(matrix4f, v2.x(), v2.y(), v2.z()).setColor(0.15f, 0.15f, 1, 1).setNormal(0, 1, 0);
            }
        }

        stack.popPose();
    }
}
