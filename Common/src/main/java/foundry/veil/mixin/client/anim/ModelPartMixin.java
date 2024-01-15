package foundry.veil.mixin.client.anim;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import foundry.veil.ext.ModelPartExtension;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(ModelPart.class)
public class ModelPartMixin implements ModelPartExtension {

    @Unique
    public float offsetX = 0;
    @Unique
    public float offsetY = 0;
    @Unique
    public float offsetZ = 0;
    @Unique
    private Supplier<ModelPart> parent = () -> null;

    @Shadow
    @Final
    private Map<String, ModelPart> children;

    @Override
    public float veil$getOffsetX() {
        return offsetX;
    }

    @Override
    public float veil$getOffsetY() {
        return offsetY;
    }

    @Override
    public float veil$getOffsetZ() {
        return offsetZ;
    }

    @Override
    public void veil$setOffset(float x, float y, float z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
    } //

    @Override
    public boolean veil$isChild(ModelPart part) {
        return this.children.containsValue(part);
    }

    @Inject(method = "translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("TAIL"))
    public void veil$rotato(PoseStack matrix, CallbackInfo ci) {
        if (this.offsetX != 0F || this.offsetY != 0F || this.offsetZ != 0F) {
            matrix.translate(this.offsetX / 16F, this.offsetY / 16F, this.offsetZ / 16F);
        }
    }

    @Inject(method = "translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"))
    public void veil$protato(PoseStack matrix, CallbackInfo ci) {
        if (this.parent.get() != null) {
            act(parent.get(), matrix);
        }
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    public void veil$copyTrans(ModelPart part, CallbackInfo ci) {
        this.offsetX = ((ModelPartExtension) (Object) part).veil$getOffsetX();
        this.offsetY = ((ModelPartExtension) (Object) part).veil$getOffsetY();
        this.offsetZ = ((ModelPartExtension) (Object) part).veil$getOffsetZ();
    }

    @Override
    public Supplier<ModelPart> veil$getParent() {
        return parent;
    }

    @Override
    public void veil$setParent(Supplier<ModelPart> parent) {
        this.parent = parent;
    }

    @Unique
    private void act(ModelPart part, PoseStack matrix) {
        matrix.translate((part.x / 16.0F), (part.y / 16.0F), (part.z / 16.0F));
        if (part.zRot != 0.0F) {
            matrix.mulPose(Axis.ZP.rotation(part.zRot));
        }

        if (part.yRot != 0.0F) {
            matrix.mulPose(Axis.YP.rotation(part.yRot));
        }

        if (part.xRot != 0.0F) {
            matrix.mulPose(Axis.XP.rotation(part.xRot));
        }

        if (part.xScale != 1.0F || part.yScale != 1.0F || part.zScale != 1.0F) {
            matrix.scale(part.xScale, part.yScale, part.zScale);
        }

        if (((ModelPartExtension) (Object) part).veil$getOffsetX() != 0F || ((ModelPartExtension) (Object) part).veil$getOffsetY() != 0F || ((ModelPartExtension) (Object) part).veil$getOffsetZ() != 0F) {
            matrix.translate((((ModelPartExtension) (Object) part).veil$getOffsetX() / 16F), (((ModelPartExtension) (Object) part).veil$getOffsetY() / 16F), (((ModelPartExtension) (Object) part).veil$getOffsetZ() / 16F));
        }
    }
}
