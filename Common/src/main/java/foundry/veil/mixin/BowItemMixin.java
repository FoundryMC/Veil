package foundry.veil.mixin;

import foundry.veil.model.anim.IChargableItem;
import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BowItem.class)
public class BowItemMixin implements IChargableItem {
    @Shadow @Final public static int MAX_DRAW_DURATION;

    @Override
    public int getMaxCharge() {
        return BowItem.MAX_DRAW_DURATION;
    }
    // no but you can override them
    @Override
    public int getCharge() {
        return ((BowItem) (Object) this).getUseDuration(((BowItem) (Object) this).getDefaultInstance());
    }

    @Override
    public void setCharge(int charge) {
        //unusable
    }

    @Override
    public void addCharge(int charge) {
        //unusable
    }

    @Override
    public void removeCharge(int charge) {
        //unusable
    }
}
