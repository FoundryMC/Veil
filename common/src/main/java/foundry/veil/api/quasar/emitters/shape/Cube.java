package foundry.veil.api.quasar.emitters.shape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import org.joml.*;

import java.lang.Math;

public class Cube implements EmitterShape {

    @Override
    public Vector3d getPoint(RandomSource randomSource, Vector3fc dimensions, Vector3fc shapeRotation, Vector3dc position, boolean fromSurface) {
        double x = randomSource.nextDouble() * 2 - 1;
        double y = randomSource.nextDouble() * 2 - 1;
        double z = randomSource.nextDouble() * 2 - 1;
        if (fromSurface) {
            int axis = randomSource.nextInt(3);
            if (axis == 0) {
                x = 2 * (randomSource.nextInt(2) - 0.5);
            } else if (axis == 1) {
                y = 2 * (randomSource.nextInt(2) - 0.5);
            } else {
                z = 2 * (randomSource.nextInt(2) - 0.5);
            }
        }
        Vector3d normal = new Vector3d(x, y, z);
        Vector3d pos = normal.mul(dimensions).mul(0.5);
        new Matrix4d().rotate(new Quaternionf().rotationXYZ((float) Math.toRadians(shapeRotation.x()), (float) Math.toRadians(shapeRotation.y()), (float) Math.toRadians(shapeRotation.z()))).transformPosition(pos);
        //pos.rotateAxis(Math.toRadians(shapeRotation.x()), 1, 0, 0);
        //pos.rotateAxis(Math.toRadians(shapeRotation.y()), 0, 1, 0);
        //pos.rotateAxis(Math.toRadians(shapeRotation.z()), 0, 0, 1);
        //pos.rotate(new Quaterniond().rotateLocalX((float) Math.toRadians(shapeRotation.x())).rotateLocalY( (float) Math.toRadians(shapeRotation.y())).rotateLocalZ( (float) Math.toRadians(shapeRotation.z())));
        return pos.add(position);
    }

    @Override
    public void renderShape(PoseStack stack, VertexConsumer consumer, Vector3fc dimensions, Vector3fc rotation) {
        stack.pushPose();

        stack.mulPose(new Quaternionf().rotationXYZ((float) Math.toRadians(rotation.x()), (float) Math.toRadians(rotation.y()), (float) Math.toRadians(rotation.z())));

        float x = dimensions.x() * 0.5f;
        float y = dimensions.y() * 0.5f;
        float z = dimensions.z() * 0.5f;
        LevelRenderer.renderLineBox(stack, consumer, new AABB(-x, -y, -z, x, y, z), 0.15f, 0.15f, 1, 1);

        stack.popPose();
    }
}
