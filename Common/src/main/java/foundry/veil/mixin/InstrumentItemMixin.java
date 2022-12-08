package foundry.veil.mixin;

import foundry.veil.model.anim.IChargableItem;
import net.minecraft.world.item.InstrumentItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(InstrumentItem.class)
public class InstrumentItemMixin implements IChargableItem {
    @Override
    public int getMaxCharge() {
        return ((InstrumentItem) (Object) this).getUseDuration(((InstrumentItem) (Object) this).getDefaultInstance());
    }

    @Override
    public int getCharge() {
        return ((InstrumentItem) (Object) this).getUseDuration(((InstrumentItem) (Object) this).getDefaultInstance());
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
