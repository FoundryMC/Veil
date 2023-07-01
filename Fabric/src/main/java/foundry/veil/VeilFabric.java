package foundry.veil;

import net.fabricmc.api.ModInitializer;

public class VeilFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Veil.init();
    }
}
