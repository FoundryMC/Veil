package foundry.veil.mixin.client.pipeline;

import org.joml.FrustumIntersection;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FrustumIntersection.class, remap = false)
public interface FrustumIntersectionAccessor {

    @Accessor
    Vector4f[] getPlanes();
}
