package foundry.veil.api.flare.data.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * @since 2.5.0
 */
public class FlareBakedQuad {
    private final float[] vertexData;
    
    public FlareBakedQuad(float[] vertexData) {
        this.vertexData = vertexData;
    }
    
    public void putBakedQuadInto(VertexConsumer buffer) {
        this.putBakedQuadInto(buffer, 1.0F, 1.0F, 1.0F, 1.0F, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }
    
    public void putBakedQuadInto(VertexConsumer buffer, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
        float[] vertexData = this.vertexData;
        
        for (int i = 0; i < 4; i++) {
            int j = i * 8;
            buffer.addVertex(vertexData[j], vertexData[j + 1], vertexData[j + 2])
                    .setNormal(vertexData[j + 3], vertexData[j + 4], vertexData[j + 5])
                    .setUv(vertexData[j + 6], vertexData[j + 7])
                    .setColor(red, green, blue, alpha)
                    .setLight(packedLight)
                    .setOverlay(packedOverlay);
            
        }
    }
    
    public void putBakedQuadInto(VertexConsumer buffer, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay) {
        float[] vertexData = this.vertexData;
        
        for (int i = 0; i < 4; i++) {
            int j = i * 8;
            buffer.addVertex(vertexData[j], vertexData[j + 1], vertexData[j + 2])
                    .setNormal(vertexData[j + 3], vertexData[j + 4], vertexData[j + 5])
                    .setUv(vertexData[j + 6], vertexData[j + 7])
                    .setColor(red, green, blue, alpha)
                    .setLight(lightmap[i])
                    .setOverlay(packedOverlay);
        }
    }
    
}
