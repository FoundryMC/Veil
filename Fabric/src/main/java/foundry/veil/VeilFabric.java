package foundry.veil;

import net.fabricmc.api.ModInitializer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class VeilFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Veil.init();
    }
}
