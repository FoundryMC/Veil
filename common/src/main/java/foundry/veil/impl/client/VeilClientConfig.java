package foundry.veil.impl.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import foundry.veil.Veil;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Small client-side config surface for compatibility switches that need to be available before render resources load.
 */
public final class VeilClientConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static VeilClientConfig INSTANCE = new VeilClientConfig();

    public boolean enableVrCompatibility = true;
    public boolean usePerEyePostProcessing = true;
    public boolean disableMotionBlurInVR = true;
    public boolean reduceDistortionInVR = true;
    public float vrPostQuality = 1.0F;
    public boolean debugVREyeBuffers = false;

    private VeilClientConfig() {
    }

    public static VeilClientConfig get() {
        return INSTANCE;
    }

    public static void load() {
        Path path = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("veil-client.json");
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                INSTANCE = new VeilClientConfig();
                save(path, INSTANCE);
                return;
            }

            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                VeilClientConfig config = GSON.fromJson(reader, VeilClientConfig.class);
                INSTANCE = config != null ? config : new VeilClientConfig();
            }
            INSTANCE.vrPostQuality = Math.max(0.25F, Math.min(1.0F, INSTANCE.vrPostQuality));
        } catch (Exception e) {
            Veil.LOGGER.warn("Failed to load Veil client config. Using defaults.", e);
            INSTANCE = new VeilClientConfig();
        }
    }

    private static void save(Path path, VeilClientConfig config) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        }
    }
}
