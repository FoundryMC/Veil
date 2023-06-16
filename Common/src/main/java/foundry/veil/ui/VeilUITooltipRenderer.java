package foundry.veil.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import foundry.veil.color.Color;
import foundry.veil.color.theme.NumberThemeProperty;
import foundry.veil.helper.SpaceHelper;
import foundry.veil.ui.anim.TooltipKeyframe;
import foundry.veil.ui.anim.TooltipTimeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;


public class VeilUITooltipRenderer {
    public static final VeilIGuiOverlay OVERLAY = VeilUITooltipRenderer::renderOverlay;

    public static int hoverTicks = 0;
    public static BlockPos lastHoveredPos = null;
    public static Vec3 currentPos = null;
    public static Vec3 desiredPos = null;

    public static void renderOverlay(Gui gui, PoseStack stack, float partialTicks, int width, int height){
        stack.pushPose();
        Minecraft mc = Minecraft.getInstance();
        if(mc.options.hideGui || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;
        HitResult result = mc.hitResult;
        if(!(result instanceof BlockHitResult)){
            hoverTicks = 0;
            lastHoveredPos = null;
            return;
        }
        BlockHitResult blockHitResult = (BlockHitResult) result;
        ClientLevel world = mc.level;
        BlockPos pos = blockHitResult.getBlockPos();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        Tooltippable tooltippable = (Tooltippable) blockEntity;
        if(tooltippable == null || !tooltippable.isTooltipEnabled()){
            hoverTicks = 0;
            lastHoveredPos = null;
            return;
        }
        int prevHoverTicks = hoverTicks;
        hoverTicks++;
        lastHoveredPos = pos;
        boolean shouldShowTooltip = VeilUITooltipHandler.shouldShowTooltip();
        List<Component> tooltip = new ArrayList<>();
        tooltip.addAll(tooltippable.getTooltip());
        if(tooltip.isEmpty()){
            hoverTicks = 0;
            return;
        }
        stack.pushPose();
        int tooltipTextWidth = 0;
        for(FormattedText line : tooltip){
            int textLineWidth = mc.font.width(line);
            if(textLineWidth > tooltipTextWidth)
                tooltipTextWidth = textLineWidth;
        }
        int tooltipHeight = 8;
        if(tooltip.size() > 1)
            tooltipHeight += 2 + (tooltip.size() - 1) * 10;
        int tooltipX = (width / 2) + 20;
        int tooltipY = (height / 2);
        int desiredX = tooltipX;
        int desiredY = tooltipY;

        tooltipX = Math.min(tooltipX, width - tooltipTextWidth - 20);
        tooltipY = Math.min(tooltipY, height - tooltipHeight - 20);

        float fade = Mth.clamp((hoverTicks + partialTicks) / 24f, 0, 1);
        Color background = tooltippable.getTheme().getColor("background");
        Color borderTop = tooltippable.getTheme().getColor("topBorder");
        Color borderBottom = tooltippable.getTheme().getColor("bottomBorder");
        float heightBonus = tooltippable.getTooltipHeight();
        float widthBonus = tooltippable.getTooltipWidth();
        float textXOffset = tooltippable.getTooltipXOffset();
        float textYOffset = tooltippable.getTooltipYOffset();
        List<VeilUIItemTooltipDataHolder> items = tooltippable.getItems();
        ItemStack istack = tooltippable.getStack() == null ? ItemStack.EMPTY : tooltippable.getStack();
        if(pos != lastHoveredPos){
            currentPos = null;
            desiredPos = null;
        }
        if(tooltippable.getWorldspace()){
            Vec3 corner = Vec3.atCenterOf(pos);
            currentPos = currentPos == null ? corner : currentPos;
            Vec3 playerPos = mc.gameRenderer.getMainCamera().getPosition();
            Vec3i playerPosInt = new Vec3i(playerPos.x, playerPos.y, playerPos.z);
            Vec3i cornerInt = new Vec3i(corner.x, corner.y, corner.z);
            Vec3i diff = playerPosInt.subtract(cornerInt);
            desiredPos = corner.add(Math.round(Mth.clamp(Math.round(diff.getX()), -1, 1) * 0.5f)-0.5f, 0.5, Math.round(Mth.clamp(Math.round(diff.getZ()), -1, 1) * 0.5f)-0.5f);
            if(fade == 0){
                currentPos = currentPos.add(0, -0.25f, 0);
                background = background.multiply(1,1,1,fade);
                borderTop = borderTop.multiply(1,1,1,fade);
                borderBottom = borderBottom.multiply(1,1,1,fade);
            }
            currentPos = currentPos.lerp(desiredPos, 0.05f);
            Vector3f screenSpacePos = SpaceHelper.worldToScreenSpace(currentPos, partialTicks);
            Vector3f desiredScreenSpacePos = SpaceHelper.worldToScreenSpace(desiredPos, partialTicks);
            screenSpacePos = new Vector3f(Mth.clamp(screenSpacePos.x(), 0, width), Mth.clamp(screenSpacePos.y(), 0, height - (mc.font.lineHeight * tooltip.size())), screenSpacePos.z());
            desiredScreenSpacePos = new Vector3f(Mth.clamp(desiredScreenSpacePos.x(), 0, width), Mth.clamp(desiredScreenSpacePos.y(), 0, height - (mc.font.lineHeight * tooltip.size())), desiredScreenSpacePos.z());
            tooltipX = (int)screenSpacePos.x();
            tooltipY = (int)screenSpacePos.y();
            desiredX = (int)desiredScreenSpacePos.x();
            desiredY = (int)desiredScreenSpacePos.y();
        }
        if(tooltippable.getTheme().getColor("connectingLine") != null) {
            stack.pushPose();
            Color color = tooltippable.getTheme().getColor("connectingLine");
            float thickness = ((NumberThemeProperty)tooltippable.getTheme().getProperty("connectingLineThickness")).getValue(Float.class);
            stack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
//            stack.mulPose(Vector3f.YP.rotationDegrees(180));
            Matrix4f mat = stack.last().pose();
            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(2);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            // draw a quad of thickness thickness from desiredX, desiredY to tooltipX, tooltipY with a z value of 399, starting from the top right corner and going anti-clockwise
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.vertex(mat, desiredX+thickness, desiredY, 399).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.vertex(mat, desiredX-thickness, desiredY, 399).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.vertex(mat, tooltipX-thickness, tooltipY, 399).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.vertex(mat, tooltipX+thickness, tooltipY, 399).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            Tesselator.getInstance().end();
            RenderSystem.disableBlend();
            RenderSystem.enableTexture();
            stack.popPose();
        }
        UIUtils.drawHoverText(partialTicks, istack, stack, tooltip, tooltipX+(int)textXOffset, tooltipY+(int)textYOffset, width, height, -1, background.getHex(), borderTop.getHex(), borderBottom.getHex(), mc.font, (int)widthBonus, (int)heightBonus, items);
        stack.popPose();
    }
}
