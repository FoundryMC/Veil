package foundry.veil.model.pose;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

public abstract class ExtendedPose implements VeilPoseable {
    public PoseData data;

    public void poseRightArm(HumanoidModel<?> model, VeilPoseable pose) {
        pose.pose(model);
    }
    @Override
    public void pose(HumanoidModel<?> model) {
        poseLeftArm(model.leftArm);
        poseRightArm(model.rightArm);
        poseLeftLeg(model.leftLeg);
        poseRightLeg(model.rightLeg);
        poseHead(model.head);
        poseBody(model.body);
        poseMainHand(data.mainHand);
        poseOffHand(data.offHand);
    }

    @Override
    public void poseMainHand(ModelPart mainHand) {

    }

    @Override
    public void poseOffHand(ModelPart offHand) {

    }

    @Override
    public void poseLeftArm(ModelPart leftArm) {

    }

    @Override
    public void poseRightArm(ModelPart rightArm) {

    }

    @Override
    public void poseLeftLeg(ModelPart leftLeg) {

    }

    @Override
    public void poseRightLeg(ModelPart rightLeg) {

    }

    @Override
    public void poseHead(ModelPart head) {

    }

    @Override
    public void poseBody(ModelPart body) {

    }
}
