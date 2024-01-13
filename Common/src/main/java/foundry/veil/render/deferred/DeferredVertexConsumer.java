package foundry.veil.render.deferred;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class DeferredVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private boolean verticalNormal;

    public DeferredVertexConsumer(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    @Override
    public VertexConsumer vertex(double pX, double pY, double pZ) {
        this.delegate.vertex(pX, pY, pZ);
        return this;
    }

    @Override
    public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
        this.delegate.color(pRed, pGreen, pBlue, pAlpha);
        return this;
    }

    @Override
    public VertexConsumer uv(float pU, float pV) {
        this.delegate.uv(pU, pV);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int pU, int pV) {
        this.delegate.overlayCoords(pU, pV);
        return this;
    }

    @Override
    public VertexConsumer uv2(int pU, int pV) {
        this.delegate.uv2(pU, pV);
        return this;
    }

    @Override
    public VertexConsumer normal(float pX, float pY, float pZ) {
        if (this.verticalNormal) {
            this.delegate.normal(0, 1, 0);
        } else {
            this.delegate.normal(pX, pY, pZ);
        }
        return this;
    }

    @Override
    public void endVertex() {
        this.delegate.endVertex();
    }

    @Override
    public void defaultColor(int pDefaultR, int pDefaultG, int pDefaultB, int pDefaultA) {
        this.delegate.defaultColor(pDefaultR, pDefaultG, pDefaultB, pDefaultA);
    }

    @Override
    public void unsetDefaultColor() {
        this.delegate.unsetDefaultColor();
    }

    @Override
    public void putBulkData(PoseStack.Pose pPoseEntry, BakedQuad pQuad, float[] pColorMuls, float pRed, float pGreen, float pBlue, int[] pCombinedLights, int pCombinedOverlay, boolean pMulColor) {
        this.verticalNormal = !pQuad.isShade();
        VertexConsumer.super.putBulkData(pPoseEntry, pQuad, pColorMuls, pRed, pGreen, pBlue, pCombinedLights, pCombinedOverlay, pMulColor);
        this.verticalNormal = false;
    }
}