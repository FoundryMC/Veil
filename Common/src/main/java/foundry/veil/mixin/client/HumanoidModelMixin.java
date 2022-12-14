package foundry.veil.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.model.anim.IChargableItem;
import foundry.veil.model.anim.OffsetModelPart;
import foundry.veil.model.pose.ExtendedPose;
import foundry.veil.model.pose.PoseData;
import foundry.veil.model.pose.PoseRegistry;
import foundry.veil.model.pose.VeilPoseable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {
    @Unique
    public VeilPoseable leftArmPose;// = PoseRegistry.TEST;
    @Unique
    public VeilPoseable rightArmPose;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void veil$poseRightArmMixin(T $$0, float $$1, float $$2, float $$3, float $$4, float $$5, CallbackInfo ci) {
        ModelPart mainHand = $$0.getMainArm() == HumanoidArm.RIGHT ? this.rightArm : this.leftArm;
        ModelPart offHand = $$0.getMainArm() == HumanoidArm.RIGHT ? this.leftArm : this.rightArm;
        ItemStack i = $$0.getMainHandItem();
        int chargeTime = i.getItem() instanceof IChargableItem ? ((IChargableItem) i.getItem()).getCharge() - Minecraft.getInstance().player.getUseItemRemainingTicks() : 1;
        int maxChargeTime = i.getItem() instanceof IChargableItem ? ((IChargableItem) i.getItem()).getMaxCharge() : 1;
        PoseData poseData = new PoseData($$3, 0, $$1, $$2, $$4, $$5, chargeTime, maxChargeTime, mainHand, offHand);
        PoseRegistry.poses.forEach((item, pose) -> {
            pose.data = poseData;
            if ($$0 instanceof Player && ((Player) $$0).getUseItem().is(item)) {
                pose.pose((HumanoidModel<?>) (Object) this);
            }
        });
    }

    @Shadow
    @Final
    public ModelPart head;

    @Shadow
    @Final
    public ModelPart body;

    @Shadow
    public boolean crouching;

    @Shadow
    @Final
    public ModelPart rightLeg;

    @Shadow
    @Final
    public ModelPart leftLeg;

    @Shadow @Final public ModelPart rightArm;

    @Shadow @Final public ModelPart leftArm;

    @ModifyExpressionValue(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/HumanoidModel;crouching:Z"))
    private boolean veil$cancelSneak(boolean original) {
        if (original) {
            this.body.xRot += 0.5f;
            this.head.xRot -= 0.5f;
            this.body.x = 3.2F;
        }
        return false;
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/HumanoidModel;body:Lnet/minecraft/client/model/geom/ModelPart;", ordinal = 4, shift = At.Shift.AFTER))
    private void veil$sneaktwo(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (this.crouching) {
            this.body.xRot += 0.5f;
            this.head.xRot -= 0.5f;
            this.body.x = 3.2F;
        }
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void veil$e(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (livingEntity instanceof Player) {
            this.body.setPos(0, 12, 0);
            ((OffsetModelPart) (Object) this.body).setOffset(0, -12, 0);
        }
    }

    @Inject(method = "translateToHand", at = @At("TAIL"))
    private void veilm$setArmAngle(HumanoidArm arm, PoseStack matrices, CallbackInfo ci) {
        this.body.translateAndRotate(matrices);
    }
}
