package foundry.veil;

import com.mojang.blaze3d.platform.InputConstants;
import foundry.veil.editor.EditorManager;
import foundry.veil.platform.services.VeilClientPlatform;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.shader.RenderTypeRegistry;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

public class VeilClient {

    private static final VeilClientPlatform PLATFORM = ServiceLoader.load(VeilClientPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Veil expected client platform implementation"));
    public static final KeyMapping EDITOR_KEY = new KeyMapping("key.veil.editor", InputConstants.Type.KEYSYM, InputConstants.KEY_GRAVE, "key.categories.veil");

    @ApiStatus.Internal
    public static void init() {
        RenderTypeRegistry.init();
    }

    @ApiStatus.Internal
    public static void initRenderer() {
        VeilRenderSystem.init();
    }

    @ApiStatus.Internal
    public static void tickClient(float partialTick) {
//        Color.tickRainbow(ticks, partialTick);
//        if (ticks % 200 == 0) {
//            OptimizationUtil.calculateStableFps();
//        }

        EditorManager editorManager = VeilRenderSystem.renderer().getEditorManager();
        while (EDITOR_KEY.consumeClick()) {
            editorManager.setEnabled(!editorManager.isEnabled());
        }
    }

    public static VeilClientPlatform clientPlatform() {
        return PLATFORM;
    }
}
