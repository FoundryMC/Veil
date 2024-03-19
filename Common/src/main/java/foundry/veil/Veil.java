package foundry.veil;

import foundry.veil.api.molang.VeilMolang;
import foundry.veil.platform.VeilPlatform;
import gg.moonflower.molangcompiler.api.MolangCompiler;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class Veil {

    public static final String MODID = "veil";
    public static final Logger LOGGER = LoggerFactory.getLogger("Veil");
    public static final boolean DEBUG;
    public static final boolean IMGUI;
    public static final boolean VERBOSE_SHADER_ERRORS;

    private static final VeilPlatform PLATFORM = ServiceLoader.load(VeilPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Veil expected platform implementation"));

    public static final boolean SODIUM = PLATFORM.isSodiumLoaded();

    static {
        DEBUG = System.getProperty("veil.debug") != null;
        IMGUI = System.getProperty("veil.disableImgui") == null && !PLATFORM.isModLoaded("axiom");
        VERBOSE_SHADER_ERRORS = System.getProperty("veil.verboseShaderErrors") != null;
    }

    @ApiStatus.Internal
    public static void init() {
        LOGGER.info("Veil is initializing.");
        if (DEBUG) {
            LOGGER.info("Veil Debug Enabled");
        }
        if (!IMGUI) {
            LOGGER.info("ImGui Disabled");
        }
        VeilMolang.set(MolangCompiler.create(MolangCompiler.DEFAULT_FLAGS, Veil.class.getClassLoader()));
    }

    public static ResourceLocation veilPath(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static VeilPlatform platform() {
        return PLATFORM;
    }
}
