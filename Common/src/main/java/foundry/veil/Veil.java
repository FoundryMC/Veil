package foundry.veil;

import foundry.veil.molang.VeilMolang;
import gg.moonflower.molangcompiler.api.MolangCompiler;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Veil {

    public static final String MODID = "veil";
    public static final String NAME = "Veil";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final boolean DEBUG;
    public static final boolean RENDER_DOC;

    static {
        DEBUG = System.getProperty("veil.debug") != null;
        RENDER_DOC = System.getProperty("veil.renderDoc") != null;
    }

    public static void init() {
        LOGGER.info("Veil is initializing.");
        if (DEBUG) {
            LOGGER.info("Veil Debug Enabled");
        }
        VeilMolang.set(MolangCompiler.create(MolangCompiler.DEFAULT_FLAGS, Veil.class.getClassLoader()));
    }

    public static ResourceLocation veilPath(String path) {
        return new ResourceLocation(MODID, path);
    }
}
