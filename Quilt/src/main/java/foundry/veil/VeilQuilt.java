package foundry.veil;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class VeilQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        Veil.init();
    }
}
