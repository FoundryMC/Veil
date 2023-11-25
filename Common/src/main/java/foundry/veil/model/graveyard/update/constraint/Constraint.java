package foundry.veil.model.graveyard.update.constraint;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.model.graveyard.attach.InterpolatedSkeletonParent;
import foundry.veil.model.graveyard.update.InterpolatedSkeleton;
import net.minecraft.client.renderer.MultiBufferSource;

public interface Constraint {
    void initialize();

    void apply();

    boolean isSatisfied();

    boolean isIterative();

    default void renderDebugInfo(InterpolatedSkeleton skeleton, InterpolatedSkeletonParent parent, float pPartialTicks, PoseStack poseStack, MultiBufferSource pBuffer) {
        return;
    }
}
