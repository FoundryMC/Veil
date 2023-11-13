package foundry.veil.quasar.emitters.modules.emitter.settings.shapes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Cube extends AbstractEmitterShape {
    @Override
    public Vec3 getPoint(RandomSource randomSource, Vec3 dimensions, Vec3 rotation, Vec3 position, boolean fromSurface) {
        double x = randomSource.nextDouble() * 2 - 1;
        double y = randomSource.nextDouble() * 2 - 1;
        double z = randomSource.nextDouble() * 2 - 1;
        double max = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
        Vec3 normal = new Vec3(x / max, y / max, z / max);
        if (!fromSurface) {
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
        float x = (float) dimensions.x();
        float y = (float) dimensions.y();
        float z = (float) dimensions.z();
        LevelRenderer.renderLineBox(stack, consumer, new AABB(-x, -y, -z, x, y, z), 0.15f, 0.15f, 1, 1);
    }
}
