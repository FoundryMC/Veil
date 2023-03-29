package foundry.veil.model.pose;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

//Unused rn
public class PoseHelper {
    public static void poseItem(float pSwingProgress, ItemStack pStack, PoseStack pMatrixStack, ItemInHandRenderer pRenderer) {
        PoseData data = new PoseData(pMatrixStack, pStack, pSwingProgress);
        PoseRegistry.poses.forEach((item, pose) -> {
            if (item == null || pose == null) return;
            pose.data = data;
            if (item.test(pStack.getItem())) {
                pose.poseItem(pRenderer);
            }
        });
    }
}
