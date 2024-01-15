package foundry.veil.api.client.tooltip.anim;

import foundry.veil.api.client.color.Color;
import net.minecraft.world.item.ItemStack;

public class TooltipKeyframe {
    private float tooltipTextHeightBonus;
    private float tooltipTextWidthBonus;
    private float tooltipTextXOffset;
    private float tooltipTextYOffset;
    private Color backgroundColor;
    private Color topBorderColor;
    private Color bottomBorderColor;
    private ItemStack itemStack;

    public TooltipKeyframe() {
    }

    public void setTooltipTextHeightBonus(float tooltipTextHeightBonus) {
        this.tooltipTextHeightBonus = tooltipTextHeightBonus;
    }

    public void setTooltipTextWidthBonus(float tooltipTextWidthBonus) {
        this.tooltipTextWidthBonus = tooltipTextWidthBonus;
    }

    public void setTooltipTextXOffset(float tooltipTextXOffset) {
        this.tooltipTextXOffset = tooltipTextXOffset;
    }

    public void setTooltipTextYOffset(float tooltipTextYOffset) {
        this.tooltipTextYOffset = tooltipTextYOffset;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setTopBorderColor(Color topBorderColor) {
        this.topBorderColor = topBorderColor;
    }

    public void setBottomBorderColor(Color bottomBorderColor) {
        this.bottomBorderColor = bottomBorderColor;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public float getTooltipTextHeightBonus() {
        return tooltipTextHeightBonus;
    }

    public float getTooltipTextWidthBonus() {
        return tooltipTextWidthBonus;
    }

    public float getTooltipTextXOffset() {
        return tooltipTextXOffset;
    }

    public float getTooltipTextYOffset() {
        return tooltipTextYOffset;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getTopBorderColor() {
        return topBorderColor;
    }

    public Color getBottomBorderColor() {
        return bottomBorderColor;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
