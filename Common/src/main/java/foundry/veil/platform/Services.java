package foundry.veil.platform;
import foundry.veil.Veil;
import foundry.veil.platform.services.VeilPlatform;

import java.util.ServiceLoader;

public class Services {

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Veil.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}