package foundry.veil.quasar.emitters.modules.emitter.settings.shapes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class Point extends AbstractEmitterShape {

    @Override
    public Vec3 getPoint(RandomSource randomSource, Vec3 dimensions, Vec3 rotation, Vec3 position, boolean fromSurface) {
        return position;
    }

    @Override
    public void renderShape(PoseStack stack, VertexConsumer consumer, Vec3 dimensions, Vec3 rotation) {

    }
}
