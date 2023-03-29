package foundry.veil.model.pose;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;

public interface VeilPoseable {
    void pose(HumanoidModel<?> model);
    void poseItem(ItemInHandRenderer itemRenderer);
    void poseLeftArm(ModelPart leftArm);
    void poseRightArm(ModelPart rightArm);
    void poseLeftLeg(ModelPart leftLeg);
    void poseRightLeg(ModelPart rightLeg);
    void poseHead(ModelPart head);
    void poseBody(ModelPart body);
    void poseMainHand(ModelPart mainHand);
    void poseOffHand(ModelPart offHand);
}
