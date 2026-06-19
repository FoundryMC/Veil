package foundry.veil.impl.client.imgui;

import com.mojang.blaze3d.platform.InputConstants;
import foundry.imgui.api.ImGuiMCEvents;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.impl.client.editor.*;
import foundry.veil.platform.VeilEventPlatform;
import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDockNodeFlags;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class VeilImGuiCompat {

    public static final KeyMapping EDITOR_KEY = new KeyMapping("key.veil.editor", InputConstants.KEY_F6, "key.categories.veil");

    private VeilImGuiCompat() {
    }

    public static void load() {
        ImGuiMCEvents.INSTANCE.imGuiLoadPre(() -> ImGui.getIO().addConfigFlags(ImGuiConfigFlags.DockingEnable | ImGuiConfigFlags.ViewportsEnable));
        ImGuiMCEvents.INSTANCE.preRenderImGuiEvent(() -> {
            VeilImGuiStylesheet.initStyles();
            AdvancedFboImGuiAreaImpl.begin();
            VeilRenderSystem.renderer().getEditorManager().render();
        });
        ImGuiMCEvents.INSTANCE.postRenderImGuiEvent(() -> {
            VeilImGuiStylesheet.initStyles();
            VeilRenderSystem.renderer().getEditorManager().renderLast();
            AdvancedFboImGuiAreaImpl.end();
        });
        VeilEventPlatform.INSTANCE.onVeilRegisterInspectors(registry -> {
            // Example for devs
            if (Veil.platform().isDevelopmentEnvironment()) {
                registry.registerInspector(new DemoInspector());
            }

            // Debug editors
            registry.registerInspector(new DeviceInfoViewer());
            // TODO fix
//            registry.registerInspector(new PipelineStatisticsViewer());
            registry.registerInspector(new PostInspector());
            registry.registerInspector(new ShaderInspector());
            registry.registerInspector(new TextureInspector());
            registry.registerInspector(new LightInspector());
            registry.registerInspector(new FramebufferInspector());
            registry.registerInspector(new ResourceManagerInspector());
            registry.registerInspector(new ParticleEditorInspector());
        });
    }
}
