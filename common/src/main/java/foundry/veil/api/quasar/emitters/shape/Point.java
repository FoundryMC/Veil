package foundry.veil.api.quasar.emitters.shape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3fc;

public class Point implements EmitterShape {

    @Override
    public Vector3d getPoint(RandomSource randomSource, Vector3fc dimensions, Vector3fc rotation, Vector3dc position, boolean fromSurface) {
        return new Vector3d(position);
    }

    @Override
    public void renderShape(PoseStack stack, VertexConsumer consumer, Vector3fc dimensions, Vector3fc rotation) {
        LevelRenderer.renderLineBox(stack, consumer, new AABB(-0.01, -0.01, -0.01, 0.01, 0.01, 0.01), 0.15f, 0.15f, 1, 1);
    }
}
