package foundry.veil.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import foundry.veil.model.anim.CrackCocaine;
import foundry.veil.model.anim.OffsetModelPart;
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
public class ModelPartMixin implements OffsetModelPart, CrackCocaine {
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
    public float getOffsetX() {
        return offsetX;
    }

    @Override
    public float getOffsetY() {
        return offsetY;
    }

    @Override
    public float getOffsetZ() {
        return offsetZ;
    }

    @Override
    public void setOffset(float x, float y, float z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
    } //

    @Override
    public boolean isChild(ModelPart part) {
        return this.children.containsValue(part);
    }

    @Inject(method = "translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("TAIL"))
    public void veil$rotato(PoseStack matrix, CallbackInfo ci) {
        if (this.offsetX != 0F || this.offsetY != 0F || this.offsetZ != 0F) {
            matrix.translate(this.offsetX/16F, this.offsetY/16F, this.offsetZ/16F);
        }
    }
    @Inject(method = "translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"))
    public void veil$protato(PoseStack matrix, CallbackInfo ci) {
        if(this.parent.get() != null) {
            act(parent.get(), matrix);
        }
    }
    @Inject(method = "copyFrom", at = @At("TAIL"))
    public void veil$copyTrans(ModelPart part, CallbackInfo ci) {
        this.offsetX = ((OffsetModelPart)(Object)part).getOffsetX();
        this.offsetY = ((OffsetModelPart)(Object)part).getOffsetY();
        this.offsetZ = ((OffsetModelPart)(Object)part).getOffsetZ();
    }

    @Override
    public Supplier<ModelPart> getParent() {
        return parent;
    }

    @Override
    public void setParent(Supplier<ModelPart> parent) {
        this.parent = parent;
    }
    @Unique
    private void act(ModelPart part, PoseStack matrix) {
        matrix.translate((double)(part.x / 16.0F), (double)(part.y / 16.0F), (double)(part.z / 16.0F));
        if (part.zRot != 0.0F) {
            matrix.mulPose(Vector3f.ZP.rotation(part.zRot));
        }

        if (part.yRot != 0.0F) {
            matrix.mulPose(Vector3f.YP.rotation(part.yRot));
        }

        if (part.xRot != 0.0F) {
            matrix.mulPose(Vector3f.XP.rotation(part.xRot));
        }

        if (part.xScale != 1.0F || part.yScale != 1.0F || part.zScale != 1.0F) {
            matrix.scale(part.xScale, part.yScale, part.zScale);
        }

        if (((OffsetModelPart)(Object)part).getOffsetX() != 0F || ((OffsetModelPart)(Object)part).getOffsetY() != 0F || ((OffsetModelPart)(Object)part).getOffsetZ() != 0F) {
            matrix.translate((((OffsetModelPart)(Object)part).getOffsetX() / 16F), (((OffsetModelPart)(Object)part).getOffsetY() / 16F), (((OffsetModelPart)(Object)part).getOffsetZ() / 16F));
        }
    }
}
