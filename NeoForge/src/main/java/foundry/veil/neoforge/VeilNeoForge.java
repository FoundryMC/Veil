package foundry.veil.neoforge;

import foundry.veil.Veil;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Mod(Veil.MODID)
public class VeilNeoForge {

    public VeilNeoForge(IEventBus modEventBus) {
        Veil.init();
        if (FMLLoader.getDist().isClient()) {
            VeilNeoForgeClient.init(modEventBus);
        }
    }
}
