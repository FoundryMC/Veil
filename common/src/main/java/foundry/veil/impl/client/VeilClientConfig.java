package foundry.veil.impl.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    public boolean disableBloomInVR = false;
    public boolean optimizeVrPerformance = true;
    public float vrBloomQuality = 0.35F;
    public float vrPostQuality = 0.6F;
    public float vrLightQuality = 0.5F;
    public boolean useSodiumCompatibleVrFallback = true;
    public boolean logVrSharedBufferWarnings = true;
    public boolean debugVREyeBuffers = false;
    public boolean debugVRPerformance = false;
    public int vrDebugLogInterval = 120;
    public boolean saveVRFrameTimeHistory = false;
    public int vrFrameTimeHistoryInterval = 1;
    public String vrFrameTimeHistoryFile = "logs/veil-vr-frame-times.csv";

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

            JsonObject json;
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
                VeilClientConfig config = GSON.fromJson(json, VeilClientConfig.class);
                INSTANCE = config != null ? config : new VeilClientConfig();
            }
            VeilClientConfig defaults = new VeilClientConfig();
            if (!json.has("enableVrCompatibility")) {
                INSTANCE.enableVrCompatibility = defaults.enableVrCompatibility;
            }
            if (!json.has("usePerEyePostProcessing")) {
                INSTANCE.usePerEyePostProcessing = defaults.usePerEyePostProcessing;
            }
            if (!json.has("disableMotionBlurInVR")) {
                INSTANCE.disableMotionBlurInVR = defaults.disableMotionBlurInVR;
            }
            if (!json.has("reduceDistortionInVR")) {
                INSTANCE.reduceDistortionInVR = defaults.reduceDistortionInVR;
            }
            if (!json.has("disableBloomInVR")) {
                INSTANCE.disableBloomInVR = defaults.disableBloomInVR;
            }
            if (!json.has("optimizeVrPerformance")) {
                INSTANCE.optimizeVrPerformance = defaults.optimizeVrPerformance;
            }
            if (!json.has("vrPostQuality") || INSTANCE.vrPostQuality <= 0.0F) {
                INSTANCE.vrPostQuality = defaults.vrPostQuality;
            }
            if (!json.has("vrBloomQuality") || INSTANCE.vrBloomQuality <= 0.0F) {
                INSTANCE.vrBloomQuality = defaults.vrBloomQuality;
            }
            if (!json.has("vrLightQuality") || INSTANCE.vrLightQuality <= 0.0F) {
                INSTANCE.vrLightQuality = defaults.vrLightQuality;
            }
            if (!json.has("useSodiumCompatibleVrFallback")) {
                INSTANCE.useSodiumCompatibleVrFallback = defaults.useSodiumCompatibleVrFallback;
            }
            if (!json.has("logVrSharedBufferWarnings")) {
                INSTANCE.logVrSharedBufferWarnings = defaults.logVrSharedBufferWarnings;
            }
            if (!json.has("debugVREyeBuffers")) {
                INSTANCE.debugVREyeBuffers = defaults.debugVREyeBuffers;
            }
            if (!json.has("debugVRPerformance")) {
                INSTANCE.debugVRPerformance = defaults.debugVRPerformance;
            }
            if (!json.has("vrDebugLogInterval") || INSTANCE.vrDebugLogInterval <= 0) {
                INSTANCE.vrDebugLogInterval = defaults.vrDebugLogInterval;
            }
            if (!json.has("saveVRFrameTimeHistory")) {
                INSTANCE.saveVRFrameTimeHistory = defaults.saveVRFrameTimeHistory;
            }
            if (!json.has("vrFrameTimeHistoryInterval") || INSTANCE.vrFrameTimeHistoryInterval <= 0) {
                INSTANCE.vrFrameTimeHistoryInterval = defaults.vrFrameTimeHistoryInterval;
            }
            if (!json.has("vrFrameTimeHistoryFile") || INSTANCE.vrFrameTimeHistoryFile == null || INSTANCE.vrFrameTimeHistoryFile.isBlank()) {
                INSTANCE.vrFrameTimeHistoryFile = defaults.vrFrameTimeHistoryFile;
            }
            INSTANCE.vrPostQuality = Math.max(0.25F, Math.min(1.0F, INSTANCE.vrPostQuality));
            INSTANCE.vrBloomQuality = Math.max(0.25F, Math.min(1.0F, INSTANCE.vrBloomQuality));
            INSTANCE.vrLightQuality = Math.max(0.25F, Math.min(1.0F, INSTANCE.vrLightQuality));
            INSTANCE.vrDebugLogInterval = Math.max(20, INSTANCE.vrDebugLogInterval);
            INSTANCE.vrFrameTimeHistoryInterval = Math.max(1, INSTANCE.vrFrameTimeHistoryInterval);
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
