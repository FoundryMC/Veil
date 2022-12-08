package foundry.veil.mixin;

import foundry.veil.model.anim.IChargableItem;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TridentItem.class)
public class TridentItemMixin implements IChargableItem {
    @Override
    public int getMaxCharge() {
        return 10;
    }

    @Override
    public int getCharge() {
        return ((TridentItem) (Object) this).getUseDuration(((TridentItem) (Object) this).getDefaultInstance());
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
