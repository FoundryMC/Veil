package foundry.veil.model.pose;

import foundry.veil.math.Easings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class PoseRegistry {
    public static Map<Predicate<Item>, ExtendedPose> poses = new HashMap<>();

    /**
     * Add a {@link ExtendedPose} to the registry.
     *
     * @param item The item that triggers the pose.
     * @param pose The pose.
     *             <p>
     *             Create an {@link ExtendedPose}, either using an anonymous class or a separate class.
     *             You are given a {@link PoseData} object which contains various state data of the model.
     **/
    public static ExtendedPose registerPose(Item item, ExtendedPose pose) {
        poses.put(i -> i.equals(item), pose);
        return pose;
    }

    public static ExtendedPose registerPose(Predicate<Item> itemPredicate, ExtendedPose pose) {
        poses.put(itemPredicate, pose);
        return pose;
    }

    public static ExtendedPose BOW = registerPose(item -> item instanceof BowItem, new ExtendedPose() {
        @Override
        public void poseMainHand(ModelPart mainHand) {
            float mult = Math.min(data.useTime + data.partialTick, data.maxUseTime) / data.maxUseTime;
            mult = Easings.ease(mult, Easings.Easing.easeInOutSine);
            mainHand.xRot *= mult;
        }

        @Override
        public void poseOffHand(ModelPart offHand) {
            float mult = Math.min(data.useTime + data.partialTick, data.maxUseTime) / data.maxUseTime;
            mult = Easings.ease(mult, Easings.Easing.easeInOutSine);
            offHand.xRot *= mult;
        }
    });

    public static ExtendedPose CROSSBOW = registerPose(i -> i instanceof CrossbowItem, new ExtendedPose() {
        @Override
        public void poseMainHand(ModelPart mainHand) {
            float mult = Math.min(data.useTime + data.partialTick, data.maxUseTime) / data.maxUseTime;
            mult = Easings.ease(mult, Easings.Easing.easeInOutSine);
            mainHand.xRot *= mult;
        }

        @Override
        public void poseOffHand(ModelPart offHand) {
            float mult = Math.min(data.useTime + data.partialTick, data.maxUseTime) / data.maxUseTime;
            mult = Easings.ease(mult, Easings.Easing.easeInOutSine);
            offHand.xRot *= mult;
        }
    });

    public static ExtendedPose SHIELD = registerPose(i -> i instanceof ShieldItem, new ExtendedPose() {
        float headXRot = 0;
        float headYRot = 0;

        @Override
        public void pose(HumanoidModel<?> model) {
            super.pose(model);
            headXRot = model.head.xRot;
            headYRot = model.head.yRot;
        }

        @Override
        public void poseItem(ItemInHandRenderer itemRenderer) {
            data.stackPoseStack.translate(0,0.25,0);
        }

        @Override
        public void poseMainHand(ModelPart offHand) {
            if (!data.swapped) {
                offHand.xRot = -((float) Math.PI / 4F) + headXRot / 3f;
                offHand.yRot = ((float) -Math.PI / 8F) + headYRot / 3f;
                float mult = Math.min(data.useTime + data.partialTick, data.maxUseTime) / data.maxUseTime;
                mult = Easings.ease(mult, Easings.Easing.easeInOutSine);
                offHand.xRot *= mult * 1.2f;
                offHand.yRot *= mult * 1.75f;
            }
        }

        @Override
        public void poseOffHand(ModelPart offHand) {
            if (data.swapped) {
                offHand.xRot = -((float) Math.PI / 4F) + headXRot / 3f;
                offHand.yRot = -((float) -Math.PI / 8F) + headYRot / 3f;
                float mult = Math.min(data.useTime + data.partialTick, data.maxUseTime) / data.maxUseTime;
                mult = Easings.ease(mult, Easings.Easing.easeInOutSine);
                offHand.xRot *= mult * 1.2f;
                offHand.yRot *= mult * 1.75f;
            }
        }
    });

    public static ExtendedPose TRIDENT = registerPose(i -> i instanceof TridentItem, new ExtendedPose() {
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
            body.xRot = -0.5f + Math.max(0, (((float) Math.PI / 2F) + headXRot)) / 2f;
        }

        @Override
        public void poseMainHand(ModelPart mainHand) {
            if (data.swapped) {
                mainHand.yRot = 0.1F + headYRot;
                mainHand.xRot = -((float) Math.PI / 2F) + headXRot;
                float mult = Math.min(data.useTime + data.partialTick, data.maxUseTime) / data.maxUseTime;
                mult = Easings.ease(mult, Easings.Easing.easeInOutSine);
                mainHand.xRot *= mult;
                mainHand.yRot *= mult;
            } else {
                mainHand.xRot = (-(float) Math.PI / 2F) + headXRot - 1.5f;
                float mult = Math.min(data.useTime + data.partialTick, data.maxUseTime) / data.maxUseTime;
                mult = Easings.ease(mult, Easings.Easing.easeInOutSine);
                if (!Minecraft.getInstance().player.isShiftKeyDown()) {
                    mainHand.xRot *= mult;
                    return;
                }
                mainHand.xRot *= mult / 2f;
                mainHand.yRot = 1.25f + headYRot;
                //mainHand.yRot *= ;
                if (data.useTime >= data.maxUseTime) {
                    mainHand.yRot += (float) (Math.sin((data.useTime + data.partialTick) / 2f) * 0.5f);
                    mainHand.xRot += (float) (Math.cos((data.useTime + data.partialTick) / 2f) * 0.5f);
                }
            }
        }

        @Override
        public void poseOffHand(ModelPart offHand) {
            if (data.swapped) {
                offHand.xRot = (-(float) Math.PI / 2F) + headXRot - 1.5f;
                float mult = Math.min(data.useTime + data.partialTick, data.maxUseTime) / data.maxUseTime;
                mult = Easings.ease(mult, Easings.Easing.easeInOutSine);
                if (!Minecraft.getInstance().player.isShiftKeyDown()) {
                    offHand.xRot *= mult;
                    return;
                }
                offHand.xRot *= mult / 2f;
                offHand.yRot = 1.25f + headYRot;
                //mainHand.yRot *= ;
                if (data.useTime >= data.maxUseTime) {
                    offHand.yRot += (float) (Math.sin((data.useTime + data.partialTick) / 2f) * 0.5f);
                    offHand.xRot += (float) (Math.cos((data.useTime + data.partialTick) / 2f) * 0.5f);
                }
            } else {
                offHand.yRot = 0.1F + headYRot;
                offHand.xRot = -((float) Math.PI / 2F) + headXRot;
                float mult = Math.min(data.useTime + data.partialTick, data.maxUseTime) / data.maxUseTime;
                mult = Easings.ease(mult, Easings.Easing.easeInOutSine);
                offHand.xRot *= mult;
                offHand.yRot *= mult;
            }
        }
    });
}
