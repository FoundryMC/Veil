package foundry.veil.quasar.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RenderUtil {
    public static void renderLineBoxScreen(PoseStack stack, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a, float scalar){
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        stack.pushPose();
//        stack.translate(0,0,-2);
        Matrix4f m4 = stack.last().pose();
        Matrix3f m3 = stack.last().normal();
        float f = (float) minX;
        float f1 = (float) minY;
        float f2 = (float) minZ;
        float f3 = (float) maxX;
        float f4 = (float) maxY;
        float f5 = (float) maxZ;
        Tesselator tessellator = Tesselator.getInstance();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        bufferbuilder.vertex(m4, f, f1, f2).color(r, g, b, a).normal(m3, 1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f3, f1, f2).color(r, g, b, a).normal(m3, 1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f, f1, f2).color(r, g, b, a).normal(m3, 0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f, f4, f2).color(r, g, b, a).normal(m3, 0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f, f1, f2).color(r, g, b, a).normal(m3, 0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(m4, f, f1, f5).color(r, g, b, a).normal(m3, 0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(m4, f3, f1, f2).color(r, g, b, a).normal(m3, 0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f3, f4, f2).color(r, g, b, a).normal(m3, 0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f3, f4, f2).color(r, g, b, a).normal(m3, -1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f, f4, f2).color(r, g, b, a).normal(m3, -1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f, f4, f2).color(r, g, b, a).normal(m3, 0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(m4, f, f4, f5).color(r, g, b, a).normal(m3, 0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(m4, f, f4, f5).color(r, g, b, a).normal(m3, 0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f, f1, f5).color(r, g, b, a).normal(m3, 0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f, f1, f5).color(r, g, b, a).normal(m3, 1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f3, f1, f5).color(r, g, b, a).normal(m3, 1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f3, f1, f5).color(r, g, b, a).normal(m3, 0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.vertex(m4, f3, f1, f2).color(r, g, b, a).normal(m3, 0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.vertex(m4, f, f4, f5).color(r, g, b, a).normal(m3, 1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f3, f4, f5).color(r, g, b, a).normal(m3, 1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f3, f1, f5).color(r, g, b, a).normal(m3, 0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f3, f4, f5).color(r, g, b, a).normal(m3, 0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(m4, f3, f4, f2).color(r, g, b, a).normal(m3, 0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(m4, f3, f4, f5).color(r, g, b, a).normal(m3, 0.0F, 0.0F, 1.0F).endVertex();
        tessellator.end();
        stack.popPose();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
    }

    public static void renderLineBoxScreen(PoseStack stack, AABB box, float r, float g, float b, float a, float scalar){
        renderLineBoxScreen(stack, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, a, scalar);
//        renderLineBoxScreen(stack, -500,-500,-500,500,500,500, 1, 0, 0, 1, scalar);
    }
}
