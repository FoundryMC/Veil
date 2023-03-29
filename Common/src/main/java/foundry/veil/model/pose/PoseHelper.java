package foundry.veil.model.pose;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.function.Predicate;

//Unused rn
public class PoseHelper {
    public static void poseItemUsing(PoseData data, ItemInHandRenderer pRenderer){
        PoseRegistry.poses.forEach((item, pose) -> {
            if (item == null || pose == null) return;
            pose.data = data;
            if (item.test(data.stack.getItem())) {
                pose.poseItemUsing(pRenderer);
            }
        });
    }

    public static void poseItem(PoseData data, ItemInHandRenderer pRenderer){
        PoseRegistry.poses.forEach((item, pose) -> {
            if (item == null || pose == null) return;
            pose.data = data;
            if (item.test(data.stack.getItem())) {
                pose.poseItem(pRenderer);
            }
        });
    }

    public static void offhandCapture(PoseData data, ItemStack pStack, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm pSide, ItemInHandRenderer pRenderer){
        for(Map.Entry<Predicate<Item>, ExtendedPose> pose : PoseRegistry.poses.entrySet()){
            if(pose.getKey().test(pStack.getItem())){
                pose.getValue().data = data;
                if(pose.getValue().forceRenderMainHand()){
                    pMatrixStack.pushPose();
                    pose.getValue().poseMainHandFirstPerson(pMatrixStack);
                    pRenderer.renderPlayerArm(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, Minecraft.getInstance().player.getMainArm());
                    pMatrixStack.popPose();
                }
                if(pose.getValue().forceRenderOffhand()){
                    pMatrixStack.pushPose();
                    pose.getValue().poseOffHandFirstPerson(pMatrixStack);
                    pRenderer.renderPlayerArm(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, Minecraft.getInstance().player.getMainArm().getOpposite());
                    pMatrixStack.popPose();
                }
            }
        }
    }
}
