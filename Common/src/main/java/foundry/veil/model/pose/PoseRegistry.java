package foundry.veil.model.pose;

import foundry.veil.math.Easings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoseRegistry {
    public static Map<Item, ExtendedPose> poses = new HashMap<>();

    /**
        * Add a {@link ExtendedPose} to the registry.
        * @param item The item that triggers the pose.
        * @param pose The pose.
        *            <p>
        * Create an {@link ExtendedPose}, either using an anonymous class or a separate class.
        * You are given a {@link PoseData} object which contains various state data of the model.
     **/
    public static ExtendedPose registerPose(Item item, ExtendedPose pose) {
        poses.put(item, pose);
        return pose;
    }

    public static ExtendedPose BOW = registerPose(Items.BOW, new ExtendedPose() {
        @Override
        public void poseMainHand(ModelPart mainHand) {
            float mult = Math.min(data.useTime+data.partialTick, data.maxUseTime)/data.maxUseTime;
            mult = Easings.ease(mult , Easings.Easing.easeInOutSine);
            mainHand.xRot *= mult;
        }

        @Override
        public void poseOffHand(ModelPart offHand) {
            float mult = Math.min(data.useTime+data.partialTick, data.maxUseTime)/data.maxUseTime;
            mult = Easings.ease(mult, Easings.Easing.easeInOutSine);
            offHand.xRot *= mult;
        }
    });

    public static ExtendedPose TRIDENT = registerPose(Items.TRIDENT, new ExtendedPose() {
        float headXRot = 0;
        float headYRot = 0;

        @Override
        public void pose(HumanoidModel<?> model) {
            super.pose(model);
            headXRot = model.head.xRot;
            headYRot = model.head.yRot;
        }

        @Override
        public void poseBody(ModelPart body) {
            body.xRot  = -0.5f + Math.max(0, (((float)Math.PI / 2F) + headXRot))/2f;
        }

        @Override
        public void poseMainHand(ModelPart mainHand) {
            mainHand.xRot  = (-(float)Math.PI / 2F) + headXRot - 1.5f;
            mainHand.yRot = -0.1F + headYRot;
            float mult = Math.min(data.useTime+data.partialTick, data.maxUseTime)/data.maxUseTime;
            mult = Easings.ease(mult , Easings.Easing.easeInOutSine);
            mainHand.xRot *= mult;
        }

        @Override
        public void poseOffHand(ModelPart offHand) {
            offHand.yRot = 0.1F + headYRot;
            offHand.xRot = -((float)Math.PI / 2F) + headXRot;
            float mult = Math.min(data.useTime+data.partialTick, data.maxUseTime)/data.maxUseTime;
            mult = Easings.ease(mult , Easings.Easing.easeInOutSine);
            offHand.xRot *= mult;
            offHand.yRot *= mult;
        }
    });
}
