package foundry.veil.ui;

import net.minecraft.world.item.ItemStack;

import java.util.function.Function;
import java.util.function.Supplier;

public class VeilUIItemTooltipDataHolder {
    private ItemStack itemStack;
    private Function<Float, Integer> x;
    private Function<Float, Integer> y;

    public VeilUIItemTooltipDataHolder(ItemStack itemStack, Function<Float, Integer> x, Function<Float, Integer> y) {
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Function<Float, Integer> getX() {
        return x;
    }

    public Function<Float, Integer> getY() {
        return y;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setX(Function<Float, Integer> x) {
        this.x = x;
    }

    public void setY(Function<Float, Integer> y) {
        this.y = y;
    }
}
