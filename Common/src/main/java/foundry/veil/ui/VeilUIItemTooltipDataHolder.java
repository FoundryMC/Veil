package foundry.veil.ui;

import net.minecraft.world.item.ItemStack;

import java.util.function.Function;
import java.util.function.Supplier;

public class VeilUIItemTooltipDataHolder {
    private ItemStack itemStack;
    private Function<Float, Float> x;
    private Function<Float, Float> y;

    public VeilUIItemTooltipDataHolder(ItemStack itemStack, Function<Float, Float> x, Function<Float, Float> y) {
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Function<Float, Float> getX() {
        return x;
    }

    public Function<Float, Float> getY() {
        return y;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setX(Function<Float, Float> x) {
        this.x = x;
    }

    public void setY(Function<Float, Float> y) {
        this.y = y;
    }
}
