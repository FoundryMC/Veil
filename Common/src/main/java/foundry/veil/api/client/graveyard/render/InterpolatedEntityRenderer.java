package foundry.veil.api.client.graveyard.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import foundry.veil.api.client.graveyard.AnimationProperties;
import foundry.veil.api.client.graveyard.skeleton.InterpolatedSkeleton;
import foundry.veil.api.client.graveyard.skeleton.InterpolatedSkeletonParent;
import foundry.veil.api.client.graveyard.skeleton.SkeletonFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class InterpolatedEntityRenderer<T extends LivingEntity & InterpolatedSkeletonParent, S extends InterpolatedSkeleton> extends EntityRenderer<T> {

    protected final SkeletonFactory<S> modelFactory;
    private final List<InterpolatedEntityRenderLayer<T, S>> layers = new ArrayList<>();

    protected InterpolatedEntityRenderer(EntityRendererProvider.Context pContext, SkeletonFactory<S> modelFactory, float shadowRadius) {
        super(pContext);
        this.modelFactory = modelFactory;
        this.shadowRadius = shadowRadius;
    }

    public void setupModelFactory(T parent) {
    }

    public final void createSkeleton(T parent) {
        this.setupModelFactory(parent);
        parent.setSkeleton(this.modelFactory.create());
    }

    public final boolean addLayer(InterpolatedEntityRenderLayer<T, S> layer) {
        return this.layers.add(layer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(T entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);
        this.setupRotations(entity, poseStack, entity.tickCount + partialTicks, partialTicks);

        Minecraft minecraft = Minecraft.getInstance();
        boolean invisible = entity.isInvisible();
        boolean isSpectatorTransparent = !invisible && !entity.isInvisibleTo(minecraft.player);
        boolean glowing = minecraft.shouldEntityAppearGlowing(entity);
        RenderType rendertype = this.getRenderType(entity, invisible, isSpectatorTransparent, glowing);
        if (rendertype != null) {
            this.renderModel(entity, partialTicks, poseStack, buffer, packedLight);
        }

        InterpolatedSkeleton skeleton = entity.getSkeleton();
        if (!entity.isSpectator()) {
            for (InterpolatedEntityRenderLayer<T, S> layer : this.layers) {
                if (skeleton != null) {
                    layer.render(poseStack, buffer, packedLight, entity, (S) skeleton, partialTicks);
                }
            }
        }

        if (skeleton != null) {
            skeleton.renderDebug(entity, poseStack, buffer, partialTicks);
        }

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    public void renderModel(T pEntity, float pPartialTicks, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight) {
        InterpolatedSkeleton skeleton = pEntity.getSkeleton();
        if (skeleton == null) {
            return;
        }

        VertexConsumer vertexconsumer = pBuffer.getBuffer(this.getRenderType(pEntity));
        int packedOverlay = LivingEntityRenderer.getOverlayCoords(pEntity, 0);
        skeleton.render(poseStack, vertexconsumer, pPackedLight, packedOverlay, pPartialTicks, 1.0F, 1.0F, 1.0F, 1.0F);
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
            float deathTime = ((float) pEntityLiving.deathTime + pPartialTicks - 1.0F) / 20.0F * 1.6F;
            deathTime = Mth.sqrt(deathTime);
            if (deathTime > 1.0F) {
                deathTime = 1.0F;
            }
            pMatrixStack.mulPose(Axis.ZP.rotationDegrees(deathTime * this.getFlipDegrees(pEntityLiving)));
        } else if (pEntityLiving.isAutoSpinAttack()) {
            pMatrixStack.mulPose(Axis.XP.rotationDegrees(-90.0F - pEntityLiving.getXRot()));
            pMatrixStack.mulPose(Axis.YP.rotationDegrees(((float) pEntityLiving.tickCount + pPartialTicks) * -75.0F));
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

    public static void tick(List<InterpolatedSkeletonParent> entitiesToRender) {
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
