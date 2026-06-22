package foundry.veil.api.quasar.emitters.shape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import org.joml.*;

import java.lang.Math;

public class Plane implements EmitterShape {

    @Override
    public Vector3d getPoint(RandomSource randomSource, Vector3fc dimensions, Vector3fc rotation, Vector3dc position, boolean fromSurface) {
        double x = randomSource.nextDouble() - 0.5;
        double y = 0;
        double z = randomSource.nextDouble() - 0.5;
        if (fromSurface) {
            if (randomSource.nextBoolean()) {
                x = randomSource.nextInt(2) - 0.5;
            } else {
                z = randomSource.nextInt(2) - 0.5;
            }
        }
        Vector3d normal = new Vector3d(x, y, z);
        Vector3d pos = normal.mul(dimensions);
        pos = pos.rotate(new Quaterniond().rotationXYZ((float) Math.toRadians(rotation.x()), (float) Math.toRadians(rotation.y()), (float) Math.toRadians(rotation.z())));
        return pos.add(position);
    }

    @Override
    public void renderShape(PoseStack stack, VertexConsumer consumer, Vector3fc dimensions, Vector3fc rotation) {
        stack.pushPose();

        stack.mulPose(new Quaternionf().rotationXYZ((float) Math.toRadians(rotation.x()), (float) Math.toRadians(rotation.y()), (float) Math.toRadians(rotation.z())));

        float x = dimensions.x() * 0.5f;
        float z = dimensions.z() * 0.5f;
        LevelRenderer.renderLineBox(stack, consumer, new AABB(-x, 0, -z, x, 0, z), 0.15f, 0.15f, 1, 1);

        stack.popPose();
    }
}
