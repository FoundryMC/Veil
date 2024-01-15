package foundry.veil.mixin.chargable;

import foundry.veil.api.ChargableItem;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TridentItem.class)
public class TridentItemMixin implements ChargableItem {

    @Override
    public int getMaxCharge() {
        return TridentItem.THROW_THRESHOLD_TIME;
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
