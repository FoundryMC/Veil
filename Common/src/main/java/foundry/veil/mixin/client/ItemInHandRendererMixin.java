package foundry.veil.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.model.anim.IChargableItem;
import foundry.veil.model.pose.PoseData;
import foundry.veil.model.pose.PoseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Unique
    private PoseData veil$poseData;

    @Unique
    private boolean overrideArmTransform = false;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    public void veil$poseItemHead(AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, CallbackInfo ci) {
        int chargeTime = pStack.getItem() instanceof IChargableItem ? ((IChargableItem) pStack.getItem()).getCharge() - Minecraft.getInstance().player.getUseItemRemainingTicks() : 1;
        int maxChargeTime = pStack.getItem() instanceof IChargableItem ? ((IChargableItem) pStack.getItem()).getMaxCharge() : 1;
        veil$poseData = new PoseData(pMatrixStack, pStack, pSwingProgress, chargeTime, maxChargeTime, pHand, pEquippedProgress);
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"))
    public void veil$poseItemUsing(AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, CallbackInfo ci) {
        overrideArmTransform = PoseHelper.poseItemUsing(veil$poseData, (ItemInHandRenderer) (Object) this);
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V", ordinal = 8))
    public void veil$poseItem(AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, CallbackInfo ci) {
        overrideArmTransform = PoseHelper.poseItem(veil$poseData, (ItemInHandRenderer) (Object) this);
    }

    @Inject(method = "applyItemArmTransform", at = @At("HEAD"), cancellable = true)
    public void veil$overrideArmTransform(PoseStack $$0, HumanoidArm $$1, float $$2, CallbackInfo ci) {
        if (overrideArmTransform) {
            ci.cancel();
        }
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z", ordinal = 1))
    public void veil$captureOffhand(AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, CallbackInfo ci) {
        PoseHelper.offhandCapture(veil$poseData, pStack, pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, pPlayer.getMainArm().getOpposite(), (ItemInHandRenderer) (Object) this);
    }
}
