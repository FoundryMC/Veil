package foundry.veil.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import foundry.veil.color.Color;
import foundry.veil.helper.SpaceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
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

import java.util.ArrayList;
import java.util.List;


public class VeilUITooltipRenderer {
    public static final VeilIGuiOverlay OVERLAY = VeilUITooltipRenderer::renderOverlay;

    public static int hoverTicks = 0;
    public static BlockPos lastHoveredPos = null;

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
        if(!(blockEntity instanceof Tooltippable)){
            hoverTicks = 0;
            lastHoveredPos = null;
            return;
        }
        Tooltippable tooltippable = (Tooltippable) blockEntity;
        int prevHoverTicks = hoverTicks;
        hoverTicks++;
        lastHoveredPos = pos;
        boolean shouldShowTooltip = VeilUITooltipHandler.shouldShowTooltip();

        boolean hasInformation = blockEntity instanceof Tooltippable;
        List<Component> tooltip = new ArrayList<>();
        if(hasInformation){
            tooltip.addAll(tooltippable.getTooltip());
        }
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

        tooltipX = Math.min(tooltipX, width - tooltipTextWidth - 20);
        tooltipY = Math.min(tooltipY, height - tooltipHeight - 20);

        float fade = Mth.clamp((hoverTicks + partialTicks) / 24f, 0, 1);
        Color background = tooltippable.getTheme().getColor("background");
        Color borderTop = tooltippable.getTheme().getColor("topBorder");
        Color borderBottom = tooltippable.getTheme().getColor("bottomBorder");

        if(fade < 1){
            stack.translate(-(Math.pow(fade, 2) * Math.signum(0.5d)*8), 0, 0);
            background = background.multiply(1,1,1,fade);
            borderTop = borderTop.multiply(1,1,1,fade);
            borderBottom = borderBottom.multiply(1,1,1,fade);
        }
        if(tooltippable.getWorldspace()){
            // translate and scale based on players position relative to the block, and rotate to face the player around the left edge
            double cameraX = mc.gameRenderer.getMainCamera().getPosition().x;
            double cameraY = mc.gameRenderer.getMainCamera().getPosition().y;
            double cameraZ = mc.gameRenderer.getMainCamera().getPosition().z;
            PoseStack modelView = RenderSystem.getModelViewStack();
            Matrix4f projMat = RenderSystem.getProjectionMatrix();

            Matrix4f viewMat = modelView.last().pose();
            Vec3 ray = TargetPicker.getRay(projMat, viewMat, mc.getWindow().getGuiScaledWidth()/2f, mc.getWindow().getGuiScaledHeight()/2f);
            Vec3 start = new Vec3(cameraX, cameraY, cameraZ);
            Vec3 end = start.add(ray.scale(100));
            if(mc.player == null) return;
            BlockHitResult blockHitResult1 = world.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
            Vec3 hitVec = blockHitResult1.getLocation();
            Vector4f hitPos = new Vector4f((float)hitVec.x, (float)hitVec.y, (float)hitVec.z, 1);
            hitPos.transform(viewMat);
            hitPos.transform(projMat);
            tooltipX = (int) ((hitPos.x() / hitPos.w() + 1) * mc.getWindow().getGuiScaledWidth() / 2);
            tooltipY = (int) ((-hitPos.y() / hitPos.w() + 1) * mc.getWindow().getGuiScaledHeight() / 2);
        }

        UIUtils.drawHoverText(ItemStack.EMPTY, stack, tooltip, tooltipX, tooltipY, width, height, -1, background.getHex(), borderTop.getHex(), borderBottom.getHex(), mc.font);
        stack.popPose();
    }
}
