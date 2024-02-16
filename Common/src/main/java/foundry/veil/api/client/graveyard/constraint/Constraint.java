package foundry.veil.api.client.graveyard.constraint;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.graveyard.skeleton.InterpolatedSkeleton;
import foundry.veil.api.client.graveyard.skeleton.InterpolatedSkeletonParent;
import net.minecraft.client.renderer.MultiBufferSource;

public interface Constraint {

    void apply();

    default void renderDebugInfo(InterpolatedSkeleton skeleton, InterpolatedSkeletonParent parent, float pPartialTicks, PoseStack poseStack, MultiBufferSource pBuffer) {
    }
}
