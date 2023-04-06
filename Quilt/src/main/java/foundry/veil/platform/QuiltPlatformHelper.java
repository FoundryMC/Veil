package foundry.veil.platform;

import foundry.veil.platform.services.IPlatformHelper;
import org.quiltmc.loader.api.QuiltLoader;

import java.nio.file.Path;

public class QuiltPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Quilt";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return QuiltLoader.isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return QuiltLoader.isDevelopmentEnvironment();
    }

    @Override
    public Path getGameDir() {
        return QuiltLoader.getGameDir();
    }
}
