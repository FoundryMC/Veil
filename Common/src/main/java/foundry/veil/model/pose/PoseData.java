package foundry.veil.model.pose;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class PoseData{
    float ageInTicks;
    float walkTime;
    float limbSwing;
    float limbSwingAmount;
    float headYaw;
    float headPitch;
    float useTime;
    float maxUseTime;
    public ModelPart mainHand;
    public ModelPart offHand;

    public boolean swapped;

    float partialTick = Minecraft.getInstance().timer.partialTick;

    public PoseData(float ageInTicks, float walkTime, float limbSwing, float limbSwingAmount, float headYaw, float headPitch, float useTime, float maxUseTime, ModelPart mainHand, ModelPart offHand, boolean swapped) {
        this.mainHand = mainHand;
        this.offHand = offHand;
        this.ageInTicks = ageInTicks;
        this.walkTime = walkTime;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.headYaw = headYaw;
        this.headPitch = headPitch;
        this.useTime = useTime;
        this.maxUseTime = maxUseTime;
        this.swapped = swapped;
    }
}
