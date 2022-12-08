package foundry.veil;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Veil {
    public static final String MODID = "veil";
    public static final String NAME = "Veil";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static void init() {
        LOGGER.info("Veil is initializing.");
    }

    public static ResourceLocation veilPath(String path) {
        return new ResourceLocation(MODID, path);
    }
}
