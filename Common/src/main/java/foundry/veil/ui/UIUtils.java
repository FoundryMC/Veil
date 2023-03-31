package foundry.veil.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UIUtils {
    public static void drawHoverText(float pticks, @Nonnull final ItemStack stack, PoseStack pStack, List<? extends FormattedText> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight,
                                     int maxTextWidth, int backgroundColor, int borderColorStart, int borderColorEnd, Font font, int tooltipTextWidthBonus, int tooltipTextHeightBonus, List<VeilUIItemTooltipDataHolder> items) {
        if(textLines.isEmpty())
            return;

        List<ClientTooltipComponent> list = gatherTooltipComponents(stack, textLines,
                stack.getTooltipImage(), mouseX, screenWidth, screenHeight, font, font);
        // RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        int tooltipTextWidth = 0;

        for (FormattedText textLine : textLines) {
            int textLineWidth = font.width(textLine);
            if (textLineWidth > tooltipTextWidth)
                tooltipTextWidth = textLineWidth;
        }

        boolean needsWrap = false;

        int titleLinesCount = 1;
        int tooltipX = mouseX + 12;
        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipTextWidth;
            if (tooltipX < 4) // if the tooltip doesn't fit on the screen
            {
                if (mouseX > screenWidth / 2)
                    tooltipTextWidth = mouseX - 12 - 8;
                else
                    tooltipTextWidth = screenWidth - 16 - mouseX;
                needsWrap = true;
            }
        }

        if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
            tooltipTextWidth = maxTextWidth;
            needsWrap = true;
        }

        if (needsWrap) {
            int wrappedTooltipWidth = 0;
            List<FormattedText> wrappedTextLines = new ArrayList<>();
            for (int i = 0; i < textLines.size(); i++) {
                FormattedText textLine = textLines.get(i);
                List<FormattedText> wrappedLine = font.getSplitter()
                        .splitLines(textLine, tooltipTextWidth, Style.EMPTY);
                if (i == 0)
                    titleLinesCount = wrappedLine.size();

                for (FormattedText line : wrappedLine) {
                    int lineWidth = font.width(line);
                    if (lineWidth > wrappedTooltipWidth)
                        wrappedTooltipWidth = lineWidth;
                    wrappedTextLines.add(line);
                }
            }
            tooltipTextWidth = wrappedTooltipWidth;
            textLines = wrappedTextLines;

            if (mouseX > screenWidth / 2)
                tooltipX = mouseX - 16 - tooltipTextWidth;
            else
                tooltipX = mouseX + 12;
        }

        int tooltipY = mouseY - 12;
        int tooltipHeight = 8;

        if (textLines.size() > 1) {
            tooltipHeight += (textLines.size() - 1) * 10;
            if (textLines.size() > titleLinesCount)
                tooltipHeight += 2; // gap between title lines and next lines
        }

        if (tooltipY < 4)
            tooltipY = 4;
        else if (tooltipY + tooltipHeight + 4 > screenHeight)
            tooltipY = screenHeight - tooltipHeight - 4;

        final int zLevel = 400;
        tooltipTextWidth += tooltipTextWidthBonus;
        tooltipHeight += tooltipTextHeightBonus;


        drawTooltipRects(pticks, pStack, zLevel, backgroundColor, borderColorStart, borderColorEnd, font, list, tooltipTextWidth, titleLinesCount, tooltipX, tooltipY, tooltipHeight, items);
    }

    private static void drawTooltipRects(float pticks, PoseStack pStack, int z, int backgroundColor, int borderColorStart, int borderColorEnd, Font font, List<ClientTooltipComponent> list, int tooltipTextWidth, int titleLinesCount, int tooltipX, int tooltipY, int tooltipHeight, List<VeilUIItemTooltipDataHolder> items) {
        pStack.pushPose();
        Matrix4f mat = pStack.last()
                .pose();
        drawGradientRect(mat, z, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3,
                tooltipY - 3, backgroundColor, backgroundColor);
        drawGradientRect(mat, z, tooltipX - 3, tooltipY + tooltipHeight + 3,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
        drawGradientRect(mat, z, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        drawGradientRect(mat, z, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3,
                backgroundColor, backgroundColor);
        drawGradientRect(mat, z, tooltipX + tooltipTextWidth + 3, tooltipY - 3,
                tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        drawGradientRect(mat, z, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1,
                tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
        drawGradientRect(mat, z, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
        drawGradientRect(mat, z, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
                tooltipY - 3 + 1, borderColorStart, borderColorStart);
        drawGradientRect(mat, z, tooltipX - 3, tooltipY + tooltipHeight + 2,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);

        MultiBufferSource.BufferSource renderType = MultiBufferSource.immediate(Tesselator.getInstance()
                .getBuilder());
        pStack.translate(0.0D, 0.0D, z);

        for (int lineNumber = 0; lineNumber < list.size(); ++lineNumber) {
            ClientTooltipComponent line = list.get(lineNumber);

            if (line != null)
                line.renderText(font, tooltipX, tooltipY, mat, renderType);

            if (lineNumber + 1 == titleLinesCount)
                tooltipY += 2;

            tooltipY += 10;
        }

        Minecraft.getInstance().getItemRenderer().blitOffset += 300;
        for (VeilUIItemTooltipDataHolder item : items) {
            Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(item.getItemStack(), tooltipX + item.getX().apply(pticks), tooltipY + item.getY().apply(pticks));
        }
        Minecraft.getInstance().getItemRenderer().blitOffset -= 300;

        renderType.endBatch();
        pStack.popPose();

        RenderSystem.enableDepthTest();
    }

    public static List<ClientTooltipComponent> gatherTooltipComponents(ItemStack stack, List<? extends FormattedText> textElements, Optional<TooltipComponent> itemComponent, int mouseX, int screenWidth, int screenHeight, @Nullable Font forcedFont, Font fallbackFont)
    {
        Font font = forcedFont != null ? forcedFont : fallbackFont;
        List<Either<FormattedText, TooltipComponent>> elements = textElements.stream()
                .map((Function<FormattedText, Either<FormattedText, TooltipComponent>>) Either::left)
                .collect(Collectors.toCollection(ArrayList::new));
        itemComponent.ifPresent(c -> elements.add(1, Either.right(c)));


        // text wrapping
        int tooltipTextWidth = elements.stream()
                .mapToInt(either -> either.map(font::width, component -> 0))
                .max()
                .orElse(0);

        boolean needsWrap = false;

        int tooltipX = mouseX + 12;
        if (tooltipX + tooltipTextWidth + 4 > screenWidth)
        {
            tooltipX = mouseX - 16 - tooltipTextWidth;
            if (tooltipX < 4) // if the tooltip doesn't fit on the screen
            {
                if (mouseX > screenWidth / 2)
                    tooltipTextWidth = mouseX - 12 - 8;
                else
                    tooltipTextWidth = screenWidth - 16 - mouseX;
                needsWrap = true;
            }
        }

//        if (tooltipTextWidth > -1)
//        {
//            tooltipTextWidth = -1;
//            needsWrap = true;
//        }

        int tooltipTextWidthF = tooltipTextWidth;
        if (needsWrap)
        {
            return elements.stream()
                    .flatMap(either -> either.map(
                            text -> font.split(text, tooltipTextWidthF).stream().map(ClientTooltipComponent::create),
                            component -> Stream.of(ClientTooltipComponent.create(component))
                    ))
                    .toList();
        }
        return elements.stream()
                .map(either -> either.map(
                        text -> ClientTooltipComponent.create(text instanceof Component ? ((Component) text).getVisualOrderText() : Language.getInstance().getVisualOrder(text)),
                        ClientTooltipComponent::create
                ))
                .toList();
    }

    public static void drawGradientRect(Matrix4f mat, int zLevel, int left, int top, int right, int bottom, int startColor, int endColor)
    {
        float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
        float startRed   = (float)(startColor >> 16 & 255) / 255.0F;
        float startGreen = (float)(startColor >>  8 & 255) / 255.0F;
        float startBlue  = (float)(startColor       & 255) / 255.0F;
        float endAlpha   = (float)(endColor   >> 24 & 255) / 255.0F;
        float endRed     = (float)(endColor   >> 16 & 255) / 255.0F;
        float endGreen   = (float)(endColor   >>  8 & 255) / 255.0F;
        float endBlue    = (float)(endColor         & 255) / 255.0F;

        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(mat, right,    top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.vertex(mat,  left,    top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.vertex(mat,  left, bottom, zLevel).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
        buffer.vertex(mat, right, bottom, zLevel).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }
}
