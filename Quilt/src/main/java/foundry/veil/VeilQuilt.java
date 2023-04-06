package foundry.veil;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class VeilQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        Veil.init();
        if(QuiltLoader.isDevelopmentEnvironment()){
        }


    }
}
