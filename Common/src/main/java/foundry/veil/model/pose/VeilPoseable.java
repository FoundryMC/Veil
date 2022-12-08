package foundry.veil.model.pose;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

public interface VeilPoseable {
    void pose(HumanoidModel<?> model);
    void poseLeftArm(ModelPart leftArm);
    void poseRightArm(ModelPart rightArm);
    void poseLeftLeg(ModelPart leftLeg);
    void poseRightLeg(ModelPart rightLeg);
    void poseHead(ModelPart head);
    void poseBody(ModelPart body);
    void poseMainHand(ModelPart mainHand);
    void poseOffHand(ModelPart offHand);
}
