package foundry.veil.helper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.color.Color;
import foundry.veil.shader.RenderTypeRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class BasicGeomHelper {
    public static RenderType TRANS_WHITE = RenderTypeRegistry.TRANSPARENT_TEXTURE.apply(new ResourceLocation("foundry", "textures/gui/white.png"));
    public static void renderCenteredQuad(PoseStack ps, VertexConsumer consumer, float size, Color color){
        ps.pushPose();
        ps.translate(-size/2, -size/2, 0);
        consumer.vertex(ps.last().pose(), 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        consumer.vertex(ps.last().pose(), size, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        consumer.vertex(ps.last().pose(), size, size, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        consumer.vertex(ps.last().pose(), 0, size, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        ps.popPose();
    }

    public static void renderCenteredQuad(PoseStack ps, VertexConsumer consumer, float width, float height, Color color){
        ps.pushPose();
        ps.translate(-width/2, -height/2, 0);
        consumer.vertex(ps.last().pose(), 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        consumer.vertex(ps.last().pose(), width, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        consumer.vertex(ps.last().pose(), width, height, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        consumer.vertex(ps.last().pose(), 0, height, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        ps.popPose();
    }
    public static void renderQuad(PoseStack ps, MultiBufferSource multiBufferSource, float width, float height, Color[] colors){
        ps.pushPose();
        VertexConsumer consumer = multiBufferSource.getBuffer(TRANS_WHITE);
        consumer.vertex(ps.last().pose(), 0, 0, 0).color(colors[0].getRed(), colors[0].getGreen(), colors[0].getBlue(), colors[0].getAlpha()).uv(0, 0).uv2(0xF000F0).endVertex();
        consumer.vertex(ps.last().pose(), width, 0, 0).color(colors[1].getRed(), colors[1].getGreen(), colors[1].getBlue(), colors[1].getAlpha()).uv(1, 0).uv2(0xF000F0).endVertex();
        consumer.vertex(ps.last().pose(), width, height, 0).color(colors[2].getRed(), colors[2].getGreen(), colors[2].getBlue(), colors[2].getAlpha()).uv(1, 1).uv2(0xF000F0).endVertex();
        consumer.vertex(ps.last().pose(), 0, height, 0).color(colors[3].getRed(), colors[3].getGreen(), colors[3].getBlue(), colors[3].getAlpha()).uv(0, 1).uv2(0xF000F0).endVertex();
        ps.popPose();
    }
    public static void renderQuad(PoseStack ps, MultiBufferSource mbuff, float width, float height, Color[] colors, RenderType shader){
        ps.pushPose();
        VertexConsumer consumer = mbuff.getBuffer(shader);
        consumer.vertex(ps.last().pose(), 0, 0, 0).color(colors[0].getRed(), colors[0].getGreen(), colors[0].getBlue(), colors[0].getAlpha()).uv(0, 0).uv2(0xF000F0).endVertex();
        consumer.vertex(ps.last().pose(), width, 0, 0).color(colors[1].getRed(), colors[1].getGreen(), colors[1].getBlue(), colors[1].getAlpha()).uv(1, 0).uv2(0xF000F0).endVertex();
        consumer.vertex(ps.last().pose(), width, height, 0).color(colors[2].getRed(), colors[2].getGreen(), colors[2].getBlue(), colors[2].getAlpha()).uv(1, 1).uv2(0xF000F0).endVertex();
        consumer.vertex(ps.last().pose(), 0, height, 0).color(colors[3].getRed(), colors[3].getGreen(), colors[3].getBlue(), colors[3].getAlpha()).uv(0, 1).uv2(0xF000F0).endVertex();
        ps.popPose();
    }
}
