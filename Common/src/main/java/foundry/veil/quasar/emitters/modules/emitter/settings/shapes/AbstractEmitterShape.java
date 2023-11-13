package foundry.veil.quasar.emitters.modules.emitter.settings.shapes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractEmitterShape {
    public abstract Vec3 getPoint(RandomSource randomSource, Vec3 dimensions, Vec3 rotation, Vec3 position, boolean fromSurface);
    public abstract void renderShape(PoseStack stack, VertexConsumer consumer, Vec3 dimensions, Vec3 rotation);
}
