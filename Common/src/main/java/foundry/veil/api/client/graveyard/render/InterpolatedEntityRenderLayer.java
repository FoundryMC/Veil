package foundry.veil.api.client.graveyard.render;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.graveyard.skeleton.InterpolatedSkeleton;
import foundry.veil.api.client.graveyard.skeleton.InterpolatedSkeletonParent;
import foundry.veil.api.client.graveyard.skeleton.SkeletonFactory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

public abstract class InterpolatedEntityRenderLayer<T extends LivingEntity & InterpolatedSkeletonParent, M extends InterpolatedSkeleton> {
    public InterpolatedEntityRenderer<T, M> renderer;

    public InterpolatedEntityRenderLayer(InterpolatedEntityRenderer<T, M> pRenderer) {
        this.renderer = pRenderer;
    }

    public abstract void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, M pSkeleton, float pPartialTicks);

    protected SkeletonFactory<M> getModelFactory() {
        return this.renderer.modelFactory;
    }
}
