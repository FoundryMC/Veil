package foundry.veil.api.client.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;

/**
 * An interface for a pose that can be applied to a model. Some data is passed to the pose, and the pose can modify the player model at runtime.
 *
 * @see ExtendedPose
 */
public interface VeilPoseable {
    /**
     * Apply the pose to the model
     *
     * @param model the model to apply the pose to
     */
    void pose(HumanoidModel<?> model);

    /**
     * Apply the pose to the item renderer
     *
     * @param itemRenderer the item renderer
     */
    void poseItem(ItemInHandRenderer itemRenderer);

    /**
     * Apply the pose to the item renderer when the player is using an item
     *
     * @param itemRenderer the item renderer
     */
    void poseItemUsing(ItemInHandRenderer itemRenderer);

    /**
     * Apply the pose to the left arm
     *
     * @param leftArm the left arm
     */
    void poseLeftArm(ModelPart leftArm);

    /**
     * Apply the pose to the right arm
     *
     * @param rightArm the right arm
     */
    void poseRightArm(ModelPart rightArm);

    /**
     * Apply the pose to the left leg
     *
     * @param leftLeg the left leg
     */
    void poseLeftLeg(ModelPart leftLeg);

    /**
     * Apply the pose to the right leg
     *
     * @param rightLeg the right leg
     */
    void poseRightLeg(ModelPart rightLeg);

    /**
     * Apply the pose to the head
     *
     * @param head the head
     */
    void poseHead(ModelPart head);

    /**
     * Apply the pose to the body
     *
     * @param body the body
     */
    void poseBody(ModelPart body);

    /**
     * Apply the pose to the main hand
     *
     * @param mainHand the main hand
     */
    void poseMainHand(ModelPart mainHand);

    /**
     * Apply the pose to the main hand when the player is in first person
     *
     * @param stack the pose stack
     */
    void poseMainHandFirstPerson(PoseStack stack);

    /**
     * Apply the pose to the off hand
     *
     * @param offHand the off hand
     */
    void poseOffHand(ModelPart offHand);

    /**
     * Apply the pose to the off hand when the player is in first person
     *
     * @param stack the pose stack
     */
    void poseOffHandFirstPerson(PoseStack stack);

    /**
     * Whether to force render the offhand in first person
     *
     * @return true if the offhand should be rendered
     */
    boolean forceRenderOffhand();

    /**
     * Whether to force render the main hand in first person
     *
     * @return true if the main hand should be rendered
     */
    boolean forceRenderMainHand();

    /**
     * Whether to override the item transform
     *
     * @return true if the item transform should be overridden
     */
    boolean overrideItemTransform();
}
