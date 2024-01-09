package foundry.veil.render;

import com.mojang.logging.LogUtils;
import foundry.veil.Veil;
import foundry.veil.render.post.PostProcessingManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;
import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * <p>Handles mixing the regular deferred pipeline and the forward-rendered transparency pipeline.</p>
 * <p>The rendering pipeline goes in this order:</p>
 * <ul>
 *     <li>Opaque Shaders</li>
 *     <li>Opaque post-processing ({@link PostProcessingManager#OPAQUE_POST})</li>
 *     <li>Light Shaders via {@link LightRenderer}</li>
 *     <li>Light post-processing ({@link PostProcessingManager#LIGHT_POST})</li>
 *     <li>Transparency Shaders ({@link PostProcessingManager#TRANSPARENT_BLIT})</li>
 *     <li>Skybox Shader(s) via {@link SkyRenderer}</li>
 *     <li>Final post-processing via {@link PostProcessingManager}</li>
 * </ul>
 *
 * @author Ocelot
 */
public class VeilDeferredRenderer implements ResourceManagerReloadListener, NativeResource {

    public static final ResourceLocation PACK_ID = Veil.veilPath("deferred");
    private static final Logger LOGGER = LogUtils.getLogger();

    private boolean enabled;

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        boolean active = resourceManager.listPacks().anyMatch(r -> r.packId().equals(PACK_ID.toString()));
        if (this.enabled == active) {
            return;
        }

        this.enabled = active;
        if (active) {
            LOGGER.info("Deferred Renderer Enabled");
            // TODO setup
        } else {
            LOGGER.info("Deferred Renderer Disabled");
            this.free();
        }
    }

    @Override
    public void free() {
        this.enabled = false;
    }

    @ApiStatus.Internal
    public void addDebugInfo(Consumer<String> consumer) {
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}
