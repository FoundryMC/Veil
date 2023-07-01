package foundry.veil.mixin;

import foundry.veil.color.Color;
import foundry.veil.color.ColorTheme;
import foundry.veil.render.ui.Tooltippable;
import foundry.veil.render.ui.VeilUIItemTooltipDataHolder;
import foundry.veil.render.ui.anim.TooltipTimeline;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements Tooltippable {
    @Unique
    private List<Component> tooltip = new ArrayList<>();

    @Unique
    private ColorTheme theme;

    @Unique
    private List<VeilUIItemTooltipDataHolder> tooltipDataHolder = new ArrayList<>();

    @Unique
    private TooltipTimeline timeline = null;

    @Unique
    private boolean worldspace = true;

    @Unique
    private boolean tooltipEnabled = false;

    @Unique
    private int tooltipX = 0;

    @Unique
    private int tooltipY = 0;

    @Unique
    private int tooltipWidth = 0;

    @Unique
    private int tooltipHeight = 0;


    @Override
    public List<Component> getTooltip() {
        return tooltip;
    }

    @Override
    public boolean isTooltipEnabled() {
        return tooltipEnabled;
    }

    @Override
    public CompoundTag saveTooltipData() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("tooltipEnabled", tooltipEnabled);
        tag.putInt("tooltipX", tooltipX);
        tag.putInt("tooltipY", tooltipY);
        tag.putInt("tooltipWidth", tooltipWidth);
        tag.putInt("tooltipHeight", tooltipHeight);
        tag.putBoolean("worldspace", worldspace);
        if(this.theme != null){
            CompoundTag themeTag = new CompoundTag();
            for(Map.Entry<Optional<String>, Color> entry : theme.getColorsMap().entrySet()){
                String key = entry.getKey().isPresent() ? entry.getKey().get() : "";
                themeTag.putInt(key, entry.getValue().getRGB());
            }
            tag.put("theme", themeTag);
        }
        return tag;
    }

    @Override
    public void loadTooltipData(CompoundTag tag) {
        tooltipEnabled = tag.getBoolean("tooltipEnabled");
        tooltipX = tag.getInt("tooltipX");
        tooltipY = tag.getInt("tooltipY");
        tooltipWidth = tag.getInt("tooltipWidth");
        tooltipHeight = tag.getInt("tooltipHeight");
        worldspace = tag.getBoolean("worldspace");
        CompoundTag themeTag = tag.getCompound("theme");
        for(String key : themeTag.getAllKeys()){
            theme.addColor(key, Color.of(themeTag.getInt(key)));
        }
    }

    @Override
    public void setTooltip(List<Component> tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void addTooltip(Component tooltip) {
        this.tooltip.add(tooltip);
    }

    @Override
    public void addTooltip(List<Component> tooltip) {
        this.tooltip.addAll(tooltip);
    }

    @Override
    public void addTooltip(String tooltip) {
        this.tooltip.add(Component.nullToEmpty(tooltip));
    }

    @Override
    public ColorTheme getTheme() {
        return theme;
    }

    @Override
    public void setTheme(ColorTheme theme) {
        this.theme = theme;
    }

    @Override
    public void setBackgroundColor(int color) {
        if(this.theme.getColor("background") != null){
            this.theme.removeColor("background");
            this.theme.addColor("background", Color.of(color));
            return;
        }
        this.theme.addColor("background", Color.of(color));
    }

    @Override
    public void setTopBorderColor(int color) {
        if(this.theme.getColor("topBorder") != null){
            this.theme.removeColor("topBorder");
            this.theme.addColor("topBorder", Color.of(color));
            return;
        }
        this.theme.addColor("topBorder", Color.of(color));
    }

    @Override
    public void setBottomBorderColor(int color) {
        if(this.theme.getColor("bottomBorder") != null){
            this.theme.removeColor("bottomBorder");
            this.theme.addColor("bottomBorder", Color.of(color));
            return;
        }
        this.theme.addColor("bottomBorder", Color.of(color));
    }

    @Override
    public boolean getWorldspace() {
        return worldspace;
    }

    @Override
    public TooltipTimeline getTimeline() {
        return timeline;
    }

    @Override
    public ItemStack getStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public int getTooltipWidth() {
        return tooltipWidth;
    }

    @Override
    public int getTooltipHeight() {
        return tooltipHeight;
    }

    @Override
    public int getTooltipXOffset() {
        return tooltipX;
    }

    @Override
    public int getTooltipYOffset() {
        return tooltipHeight;
    }

    @Override
    public List<VeilUIItemTooltipDataHolder> getItems() {
        return tooltipDataHolder;
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    public void saveAdditional(CompoundTag $$0, CallbackInfo ci){
        $$0.put("tooltipData", saveTooltipData());
    }

    @Inject(method = "load", at = @At("RETURN"))
    public void loadAdditional(CompoundTag $$0, CallbackInfo ci){
        loadTooltipData($$0.getCompound("tooltipData"));
    }
    
}
