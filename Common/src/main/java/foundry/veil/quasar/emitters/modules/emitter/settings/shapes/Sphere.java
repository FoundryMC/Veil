package foundry.veil.quasar.emitters.modules.emitter.settings.shapes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Sphere extends AbstractEmitterShape {
    @Override
    public Vec3 getPoint(RandomSource randomSource, Vec3 dimensions, Vec3 rotation, Vec3 position, boolean fromSurface) {
        double x = randomSource.nextDouble() * 2 - 1;
        double y = randomSource.nextDouble() * 2 - 1;
        double z = randomSource.nextDouble() * 2 - 1;
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
        return pos.add(position);    }

    @Override
    public void renderShape(PoseStack stack, VertexConsumer consumer, Vec3 dimensions, Vec3 rotation) {
        float radius = (float) dimensions.x();
        Matrix4f matrix4f = stack.last().pose();
        for(int i = 0; i < 32; i++){
            for(int j = 0; j < 32; j++){
                Vector3f v1 = parametricSphere((float) Math.toRadians(i * 11.25f), (float) Math.toRadians(j * 11.25f), radius);
                Vector3f v2 = parametricSphere((float) Math.toRadians((i + 1) * 11.25f), (float) Math.toRadians(j * 11.25f), radius);
                Vector3f v3 = parametricSphere((float) Math.toRadians(i * 11.25f), (float) Math.toRadians((j + 1) * 11.25f), radius);
                Vector3f v4 = parametricSphere((float) Math.toRadians((i + 1) * 11.25f), (float) Math.toRadians((j + 1) * 11.25f), radius);
                consumer.vertex(matrix4f, v1.x(), v1.y(), v1.z()).color(0.15f, 0.15f, 1, 1).normal(0, 1, 0).endVertex();
                consumer.vertex(matrix4f, v2.x(), v2.y(), v2.z()).color(0.15f, 0.15f, 1, 1).normal(0, 1, 0).endVertex();
                consumer.vertex(matrix4f, v3.x(), v3.y(), v3.z()).color(0.15f, 0.15f, 1, 1).normal(0, 1, 0).endVertex();
                consumer.vertex(matrix4f, v4.x(), v4.y(), v4.z()).color(0.15f, 0.15f, 1, 1).normal(0, 1, 0).endVertex();
            }
        }
    }
    public static Vector3f parametricSphere(float u, float v, float r) {
        return new Vector3f(Mth.cos(u) * Mth.sin(v) * r, Mth.cos(v) * r, Mth.sin(u) * Mth.sin(v) * r);
    }
}
