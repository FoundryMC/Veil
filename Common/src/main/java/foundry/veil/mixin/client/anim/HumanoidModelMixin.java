package foundry.veil.mixin.client.anim;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.ChargableItem;
import foundry.veil.api.client.pose.PoseData;
import foundry.veil.api.client.pose.VeilPoseable;
import foundry.veil.api.client.registry.PoseRegistry;
import foundry.veil.ext.AgeableListModelExtension;
import foundry.veil.ext.ModelPartExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
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
public class HumanoidModelMixin<T extends LivingEntity> implements AgeableListModelExtension {

    @Unique
    public VeilPoseable leftArmPose;// = PoseRegistry.TEST;
    @Unique
    public VeilPoseable rightArmPose;

    @Unique
    public boolean hasActivePose;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void veil$poseRightArmMixin(T $$0, float $$1, float $$2, float $$3, float $$4, float $$5, CallbackInfo ci) {
        this.hasActivePose = false;
        ItemStack i = $$0.getMainHandItem();
        int chargeTime = i.getItem() instanceof ChargableItem ? ((ChargableItem) i.getItem()).getCharge() - Minecraft.getInstance().player.getUseItemRemainingTicks() : 1;
        int maxChargeTime = i.getItem() instanceof ChargableItem ? ((ChargableItem) i.getItem()).getMaxCharge() : 1;
        ModelPart mainhand = $$0.getMainArm() == HumanoidArm.RIGHT ? this.rightArm : this.leftArm;
        ModelPart offhand = $$0.getMainArm() == HumanoidArm.RIGHT ? this.leftArm : this.rightArm;
        boolean swapped = $$0.getUsedItemHand() == InteractionHand.OFF_HAND;
        // swap arms if offhand
        PoseData poseData = new PoseData($$3, 0, $$1, $$2, $$4, $$5, chargeTime, maxChargeTime, mainhand, offhand, swapped);
        PoseRegistry.poses.forEach((item, pose) -> {
            if (item == null || pose == null) return;
            pose.data = poseData;
            if ($$0 instanceof Player && item.test($$0.getUseItem().getItem())) {
                pose.pose((HumanoidModel<?>) (Object) this);
                this.hasActivePose = true;
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
    public ModelPart rightArm;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Unique
    private boolean veil$prevCrouch;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/HumanoidModel;crouching:Z", shift = At.Shift.BEFORE))
    private void veil$cancelSneak(T $$0, float $$1, float $$2, float $$3, float $$4, float $$5, CallbackInfo ci) {
        this.veil$prevCrouch = this.crouching;

        if (this.hasActivePose) this.crouching = false;
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/HumanoidModel;rightArmPose:Lnet/minecraft/client/model/HumanoidModel$ArmPose;", ordinal = 1, shift = At.Shift.BEFORE))
    private void veil$cancelSneak2(T $$0, float $$1, float $$2, float $$3, float $$4, float $$5, CallbackInfo ci) {
        this.crouching = this.veil$prevCrouch;
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/HumanoidModel;body:Lnet/minecraft/client/model/geom/ModelPart;", ordinal = 4, shift = At.Shift.AFTER))
    private void veil$sneaktwo(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (this.crouching && this.hasActivePose) {
            this.body.xRot += 0.5f;
            this.head.xRot -= 0.5f;
            this.body.x = 3.2F;
        } else {
            this.body.x = 0;
        }
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void veil$e(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (livingEntity instanceof Player && this.hasActivePose) {
            this.body.setPos(0, 12, 0);
            ((ModelPartExtension) (Object) this.body).veil$setOffset(0, -12, 0);
        } else {
            this.body.setPos(0, 0, 0);
            ((ModelPartExtension) (Object) this.body).veil$setOffset(0, 0, 0);
        }
    }

    @Inject(method = "translateToHand", at = @At("TAIL"))
    private void veilm$setArmAngle(HumanoidArm arm, PoseStack matrices, CallbackInfo ci) {
        if (this.hasActivePose) {
            this.body.translateAndRotate(matrices);
        }
    }

    @Override
    public boolean veil$isPosing() {
        return this.hasActivePose;
    }
}
