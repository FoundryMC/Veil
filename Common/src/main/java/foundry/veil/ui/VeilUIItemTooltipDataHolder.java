package foundry.veil.ui;

import net.minecraft.world.item.ItemStack;

public class VeilUIItemTooltipDataHolder {
    private ItemStack itemStack;
    private int x;
    private int y;

    public VeilUIItemTooltipDataHolder(ItemStack itemStack, int x, int y) {
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
