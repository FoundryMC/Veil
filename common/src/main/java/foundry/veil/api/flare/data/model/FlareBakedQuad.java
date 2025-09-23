package foundry.veil.api.flare.data.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

public record FlareBakedQuad(float[] vertexData, Vector3fc normal) {

    public void putBakedQuadInto(VertexConsumer buffer, PoseStack.Pose pose) {
        this.putBakedQuadInto(buffer, pose, 1.0f, 1.0f, 1.0f, 1.0f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    public void putBakedQuadInto(VertexConsumer buffer, PoseStack.Pose pose, Vector4fc color) {
        this.putBakedQuadInto(buffer, pose, color.x(), color.y(), color.z(), color.w(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    public void putBakedQuadInto(VertexConsumer buffer, PoseStack.Pose pose, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
        float[] vertexData = this.vertexData();
        Vector3fc normal = this.normal();

        for (int i = 0; i < 4; i++) {
            int j = i*5;
            buffer.addVertex(pose, vertexData[j], vertexData[j + 1], vertexData[j + 2])
                    .setUv(vertexData[j + 3], vertexData[j + 4])
                    .setNormal(pose, normal.x(), normal.y(), normal.z())
                    .setColor(red, green, blue, alpha)
                    .setLight(packedLight)
                    .setOverlay(packedOverlay);

        }

    }

    public void putBakedQuadInto(VertexConsumer buffer, PoseStack.Pose pose, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay) {
        float[] vertexData = this.vertexData();
        Vector3fc normal = this.normal();

        for (int i = 0; i < 4; i++) {
            int j = i*5;
            buffer.addVertex(pose, vertexData[j], vertexData[j + 1], vertexData[j + 2])
                    .setUv(vertexData[j + 3], vertexData[j + 4])
                    .setNormal(pose, normal.x(), normal.y(), normal.z())
                    .setColor(red, green, blue, alpha)
                    .setLight(lightmap[i])
                    .setOverlay(packedOverlay);

        }

    }
}
