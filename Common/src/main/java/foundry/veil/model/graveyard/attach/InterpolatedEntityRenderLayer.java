package foundry.veil.model.graveyard.attach;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.model.graveyard.update.InterpolatedSkeleton;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

public abstract class InterpolatedEntityRenderLayer<T extends LivingEntity & InterpolatedSkeletonParent, M extends InterpolatedSkeleton> {
    public InterpolatedEntityRenderer<T, M> renderer;

    public InterpolatedEntityRenderLayer(InterpolatedEntityRenderer<T, M> pRenderer) {
        this.renderer = pRenderer;
    }

    public abstract void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, M pSkeleton, float pPartialTicks);

    protected SkeletonFactory getModelFactory() {
        return this.renderer.modelFactory;
    }
}
