package foundry.veil.mixin.chargable;

import foundry.veil.api.ChargableItem;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin implements ChargableItem {

    @Override
    public int getMaxCharge() {
        return 25;
    }

    @Override
    public int getCharge() {
        return ((CrossbowItem) (Object) this).getUseDuration(((CrossbowItem) (Object) this).getDefaultInstance());
    }

    @Override
    public void setCharge(int charge) {

    }

    @Override
    public void addCharge(int charge) {

    }

    @Override
    public void removeCharge(int charge) {

    }
}
