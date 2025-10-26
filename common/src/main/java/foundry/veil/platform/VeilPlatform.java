package foundry.veil.platform;

/**
 * Manages common platform-specific features.
 */
public interface VeilPlatform {

    /**
     * @return The detected platform operand
     */
    PlatformType getPlatformType();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * @return Whether it is possible to attach renderdoc
     */
    boolean canAttachRenderdoc();

    /**
     * @return Whether the mod loader has errors and cannot load
     */
    boolean hasErrors();

    enum PlatformType {
        NEOFORGE("NeoForge", "forge"),
        FABRIC("Fabric", "fabric");

        private final String platformName;
        private final String mixinPackageName;

        PlatformType(String platformName, String mixinPackageName) {
            this.platformName = platformName;
            this.mixinPackageName = mixinPackageName;
        }

        /**
         * @return The name of the current platform
         */
        public String getPlatformName() {
            return this.platformName;
        }

        /**
         * @return The mixin package name
         */
        public String getMixinPackageName() {
            return this.mixinPackageName;
        }
    }
}
