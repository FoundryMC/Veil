package foundry.veil.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.model.anim.CrackCocaine;
import foundry.veil.model.anim.IPoseable;
import foundry.veil.model.anim.OffsetModelPart;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(AgeableListModel.class)
public class AgeableListModelMixin {
    
    @Unique
    Supplier<PoseStack> matrices;
    @Unique
    VertexConsumer vertices;
    @Unique
    int light;
    @Unique
    int overlay;
    @Unique
    float red;
    @Unique
    float green;
    @Unique
    float blue;
    @Unique
    float alpha;

    @Inject(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", at = @At("HEAD"))
    private void veil$cursedhijackery(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
        this.matrices = () -> matrices;
        this.vertices = vertices;
        this.light = light;
        this.overlay = overlay;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    @ModifyArg(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;forEach(Ljava/util/function/Consumer;)V"))
    private Consumer<? super ModelPart> veil$doublethecursed(Consumer<? super ModelPart> action) {
        if (((AgeableListModel) (Object) this) instanceof HumanoidModel<?> biped && !(biped instanceof EndermanModel)) {
            if (((IPoseable) biped).isPosing()) {
                return ((Consumer<ModelPart>) (headPart) -> {
                    PoseStack temp = matrices.get();
                    temp.pushPose();
                    boolean b = headPart.equals(biped.leftArm) || headPart.equals(biped.head) || headPart.equals(biped.rightArm) || headPart.equals(biped.hat) || ((OffsetModelPart) (Object) biped.rightArm).isChild(headPart) || ((OffsetModelPart) (Object) biped.head).isChild(headPart) || ((OffsetModelPart) (Object) biped.leftArm).isChild(headPart);
                    if (biped instanceof PlayerModel<?> p && !b) {
                        b = headPart.equals(p.leftSleeve) || headPart.equals(p.rightSleeve);
                    }
                    if (b && ((CrackCocaine) (Object) headPart).getParent().get() == null) {
                        ((CrackCocaine) (Object) headPart).setParent(() -> biped.body);
                    }
                    headPart.render(temp, vertices, light, overlay, red, green, blue, alpha);
                    temp.popPose();
                });
            } else {
                return ((Consumer<ModelPart>) (headPart) -> {
                    PoseStack temp = matrices.get();
                    temp.pushPose();
                    boolean b = headPart.equals(biped.leftArm) || headPart.equals(biped.head) || headPart.equals(biped.rightArm) || headPart.equals(biped.hat) || ((OffsetModelPart) (Object) biped.rightArm).isChild(headPart) || ((OffsetModelPart) (Object) biped.head).isChild(headPart) || ((OffsetModelPart) (Object) biped.leftArm).isChild(headPart);
                    if (biped instanceof PlayerModel<?> p && !b) {
                        b = headPart.equals(p.leftSleeve) || headPart.equals(p.rightSleeve);
                    }
                    ((CrackCocaine) (Object) headPart).setParent(() -> null);
                    headPart.render(temp, vertices, light, overlay, red, green, blue, alpha);
                    temp.popPose();
                });
            }
        }
        return action;
    }
}
