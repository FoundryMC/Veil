package foundry.veil;

import foundry.veil.api.molang.VeilMolang;
import foundry.veil.platform.services.VeilPlatform;
import gg.moonflower.molangcompiler.api.MolangCompiler;
import imgui.ImGui;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class Veil {

    public static final String MODID = "veil";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final boolean DEBUG;
    public static final boolean IMGUI;

    private static final VeilPlatform PLATFORM = ServiceLoader.load(VeilPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Veil expected platform implementation"));

    static {
        DEBUG = System.getProperty("veil.debug") != null;
        IMGUI = System.getProperty("veil.disableImgui") == null && hasImguiNatives();
    }

    private static boolean hasImguiNatives() {
        String libName = System.getProperty("os.arch").contains("64") ? "imgui-java64" : "imgui-java";
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        String name = System.mapLibraryName(windows ? libName : ("lib" + libName));
        return ImGui.class.getClassLoader().getResource("io/imgui/java/native-bin/" + name) != null;
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
