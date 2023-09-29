package foundry.veil;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(Veil.MODID)
public class VeilForge {

    public VeilForge() {
        Veil.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> VeilForgeClient::init);
    }
}
