package foundry.veil.api.client.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;

/**
 * An abstract class for a pose that can be applied to a model. Some data is passed to the pose, and the pose can modify the player model at runtime.
 *
 * @see VeilPoseable
 */
public abstract class ExtendedPose implements VeilPoseable {

    public PoseData data;

    @Override
    public void pose(HumanoidModel<?> model) {
        this.poseLeftArm(model.leftArm);
        this.poseRightArm(model.rightArm);
        this.poseLeftLeg(model.leftLeg);
        this.poseRightLeg(model.rightLeg);
        this.poseHead(model.head);
        this.poseBody(model.body);
        this.poseMainHand(this.data.mainHand);
        this.poseOffHand(this.data.offHand);
    }

    @Override
    public boolean overrideItemTransform() {
        return false;
    }

    @Override
    public boolean forceRenderOffhand() {
        return false;
    }

    @Override
    public boolean forceRenderMainHand() {
        return false;
    }

    @Override
    public void poseItemUsing(ItemInHandRenderer itemRenderer) {

    }

    @Override
    public void poseMainHandFirstPerson(PoseStack stack) {

    }

    @Override
    public void poseOffHandFirstPerson(PoseStack stack) {

    }

    @Override
    public void poseItem(ItemInHandRenderer itemRenderer) {
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
