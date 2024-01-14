package foundry.veil.neoforge.platform;

import foundry.veil.platform.services.VeilPlatform;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NeoForgeVeilPlatform implements VeilPlatform {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }
}
