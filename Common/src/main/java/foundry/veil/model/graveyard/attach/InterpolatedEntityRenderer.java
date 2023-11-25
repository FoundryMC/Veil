package foundry.veil.model.graveyard.attach;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import foundry.veil.model.graveyard.update.AnimationProperties;
import foundry.veil.model.graveyard.update.InterpolatedSkeleton;
import foundry.veil.model.graveyard.update.constraint.Constraint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public abstract class InterpolatedEntityRenderer<T extends LivingEntity & InterpolatedSkeletonParent, M extends InterpolatedSkeleton> extends EntityRenderer<T> {
    protected final SkeletonFactory modelFactory;
    List<InterpolatedEntityRenderLayer<T, M>> layers = Lists.newArrayList();

    protected InterpolatedEntityRenderer(EntityRendererProvider.Context pContext, SkeletonFactory modelFactory, float shadowRadius) {
        super(pContext);
        this.modelFactory = modelFactory;
        this.shadowRadius = shadowRadius;
    }

    public void setupModelFactory(T parent) {}

    public final void createSkeleton(T parent) {
        this.setupModelFactory(parent);
        parent.setSkeleton(this.modelFactory.create());
    }

    public final boolean addLayer(InterpolatedEntityRenderLayer<T, M> layer) {
        return this.layers.add(layer);
    }

    public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight) {
        poseStack.pushPose();
        float scale = 1.0F / 16.0F;
        poseStack.scale(scale, scale, scale);
        this.setupRotations(pEntity, poseStack, pEntity.tickCount + pPartialTicks, pPartialTicks);

        Minecraft minecraft = Minecraft.getInstance();
        boolean invisible = pEntity.isInvisible();
        boolean isSpectatorTransparent = !invisible && !pEntity.isInvisibleTo(minecraft.player);
        boolean glowing = minecraft.shouldEntityAppearGlowing(pEntity);
        RenderType rendertype = this.getRenderType(pEntity, invisible, isSpectatorTransparent, glowing);
        if (rendertype != null) {
            renderModel(pEntity, pPartialTicks, poseStack, pBuffer, pPackedLight);
        }

        if (!pEntity.isSpectator()) {
            for(InterpolatedEntityRenderLayer<T, M> layer : this.layers) {
                if (pEntity.getSkeleton() != null) layer.render(poseStack, pBuffer, pPackedLight, pEntity, (M) pEntity.getSkeleton(), pPartialTicks);
            }
        }

        if (pEntity.getSkeleton() != null) {
            for (Object obj : pEntity.getSkeleton().constraints) {
                if (obj instanceof Constraint constraint) {
                    constraint.renderDebugInfo(pEntity.getSkeleton(), pEntity, pPartialTicks, poseStack, pBuffer);
                }
            }
        }

        poseStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, poseStack, pBuffer, pPackedLight);
    }

    public void renderModel(T pEntity, float pPartialTicks, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight) {
        VertexConsumer vertexconsumer = pBuffer.getBuffer(this.getRenderType(pEntity));
        int packedOverlay = LivingEntityRenderer.getOverlayCoords(pEntity, 0);

        if (pEntity.getSkeleton() != null) pEntity.getSkeleton().render(pPartialTicks, poseStack, vertexconsumer, pPackedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public abstract RenderType getRenderType(T entity);

    protected RenderType getRenderType(T pLivingEntity, boolean pBodyVisible, boolean pTranslucent, boolean pGlowing) {
        ResourceLocation resourcelocation = this.getTextureLocation(pLivingEntity);
        if (pTranslucent) {
            return RenderType.itemEntityTranslucentCull(resourcelocation);
        } else if (pBodyVisible) {
            return this.getRenderType(pLivingEntity);
        } else {
            return pGlowing ? RenderType.outline(resourcelocation) : null;
        }
    }

    protected void setupRotations(T pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pPartialTicks) {
        if (pEntityLiving.deathTime > 0) {
            float deathTime = ((float)pEntityLiving.deathTime + pPartialTicks - 1.0F) / 20.0F * 1.6F;
            deathTime = Mth.sqrt(deathTime);
            if (deathTime > 1.0F) {
                deathTime = 1.0F;
            }
            pMatrixStack.mulPose(Axis.ZP.rotationDegrees(deathTime * this.getFlipDegrees(pEntityLiving)));
        } else if (pEntityLiving.isAutoSpinAttack()) {
            pMatrixStack.mulPose(Axis.XP.rotationDegrees(-90.0F - pEntityLiving.getXRot()));
            pMatrixStack.mulPose(Axis.YP.rotationDegrees(((float)pEntityLiving.tickCount + pPartialTicks) * -75.0F));
        } else if (LivingEntityRenderer.isEntityUpsideDown(pEntityLiving)) {
            pMatrixStack.translate(0.0D, pEntityLiving.getBbHeight() + 0.1F, 0.0D);
            pMatrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
        if (pEntityLiving.isFullyFrozen()) {
            pMatrixStack.mulPose(Axis.YP.rotationDegrees(Mth.cos(pEntityLiving.tickCount * 3.25F) * Mth.PI * 0.4F));
        }
    }

    protected float getFlipDegrees(T entity) {
        return 90.0F;
    }

    public static <M extends InterpolatedSkeleton, T extends LivingEntity & InterpolatedSkeletonParent> void tick(List<InterpolatedSkeletonParent> entitiesToRender) {
        for (InterpolatedSkeletonParent interpolatedSkeletonParent : entitiesToRender) {
            AnimationProperties properties = new AnimationProperties();
            interpolatedSkeletonParent.getSkeleton().addAnimationProperties(properties, interpolatedSkeletonParent);
            interpolatedSkeletonParent.getSkeleton().tick(properties);
        }

        // TODO: MULTITHREAD ANIMATION CALCULATION AND EXECUTION?
//        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
//        for (InterpolatedSkeletonParent interpolatedSkeletonParent : entitiesToRender) {
//            executor.submit(new UpdateEntityTask(interpolatedSkeletonParent));
//        }
//
//        //maximum time of 1 tick
//        try {
//            executor.awaitTermination(50, TimeUnit.MILLISECONDS);
//        } catch (InterruptedException e) {
//            Clinker.LOGGER.warn("! Abandoned animation task early !");
//            Clinker.LOGGER.warn(e.getLocalizedMessage());
//        }
    }

//    private record UpdateEntityTask(InterpolatedSkeletonParent animator) implements Runnable {
//        @Override
//        public void run() {
//            AnimationProperties properties = new AnimationProperties();
//            animator.getSkeleton().addAnimationProperties(properties, animator);
//            animator.getSkeleton().tick(properties);
//        }
//    }
}
