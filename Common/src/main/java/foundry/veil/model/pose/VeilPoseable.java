package foundry.veil.model.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;

public interface VeilPoseable {
    void pose(HumanoidModel<?> model);
    void poseItem(ItemInHandRenderer itemRenderer);
    void poseItemUsing(ItemInHandRenderer itemRenderer);
    void poseLeftArm(ModelPart leftArm);
    void poseRightArm(ModelPart rightArm);
    void poseLeftLeg(ModelPart leftLeg);
    void poseRightLeg(ModelPart rightLeg);
    void poseHead(ModelPart head);
    void poseBody(ModelPart body);
    void poseMainHand(ModelPart mainHand);
    void poseMainHandFirstPerson(PoseStack stack);
    void poseOffHand(ModelPart offHand);
    void poseOffHandFirstPerson(PoseStack stack);
    boolean forceRenderOffhand();
    boolean forceRenderMainHand();
    boolean overrideItemTransform();
}
