package foundry.veil;

import com.mojang.blaze3d.platform.InputConstants;
import foundry.veil.api.client.editor.EditorManager;
import foundry.veil.api.client.registry.LightTypeRegistry;
import foundry.veil.api.client.registry.PostPipelineStageRegistry;
import foundry.veil.api.client.registry.RenderTypeStageRegistry;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.impl.client.editor.*;
import foundry.veil.platform.VeilClientPlatform;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

public class VeilClient {

    private static final VeilClientPlatform PLATFORM = ServiceLoader.load(VeilClientPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Veil expected client platform implementation"));
    public static final KeyMapping EDITOR_KEY = new KeyMapping("key.veil.editor", InputConstants.Type.KEYSYM, InputConstants.KEY_F6, "key.categories.veil");

    @ApiStatus.Internal
    public static void init() {
        VeilEventPlatform.INSTANCE.onFreeNativeResources(VeilRenderSystem::close);
        VeilEventPlatform.INSTANCE.onVeilRendererAvailable(renderer -> {
            if (renderer.hasImGui()) {
                EditorManager editorManager = renderer.getEditorManager();

                // debug editors
                editorManager.add(new ExampleEditor());
                editorManager.add(new PostEditor());
                editorManager.add(new ShaderEditor());
                editorManager.add(new TextureEditor());
                editorManager.add(new OpenCLEditor());
                editorManager.add(new DeviceInfoViewer());
                editorManager.add(new DeferredEditor());
                editorManager.add(new LightEditor());
            }
        });
        // This fixes moving transparent blocks drawing too early
        VeilEventPlatform.INSTANCE.onVeilRegisterFixedBuffers(registry -> registry.registerFixedBuffer(VeilRenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS, RenderType.translucentMovingBlock()));
        RenderTypeStageRegistry.addGenericStage(renderType -> true, new RenderStateShard(Veil.MODID + ":deferred", () -> VeilRenderSystem.renderer().getDeferredRenderer().setup(), () -> VeilRenderSystem.renderer().getDeferredRenderer().clear()) {
        });
        PostPipelineStageRegistry.bootstrap();
        LightTypeRegistry.bootstrap();
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
    }

    public static VeilClientPlatform clientPlatform() {
        return PLATFORM;
    }
}
