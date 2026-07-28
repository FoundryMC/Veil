package foundry.veil;

import foundry.veil.api.client.registry.*;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.QuasarParticles;
import foundry.veil.api.quasar.registry.EmitterShapeRegistry;
import foundry.veil.api.quasar.registry.RenderStyleRegistry;
import foundry.veil.impl.client.imgui.VeilImGuiCompat;
import foundry.veil.impl.client.render.dynamicbuffer.DynamicBufferManager;
import foundry.veil.impl.client.render.dynamicbuffer.DynamicBufferShard;
import foundry.veil.impl.flare.FlareManager;
import foundry.veil.impl.quasar.QuasarParticleHandler;
import foundry.veil.impl.resource.VeilResourceManagerImpl;
import foundry.veil.platform.VeilClientPlatform;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

@ApiStatus.Internal
public class VeilClient {

    private static final VeilClientPlatform PLATFORM = ServiceLoader.load(VeilClientPlatform.class).findFirst().orElseThrow(() -> new RuntimeException("Veil expected client platform implementation"));
    private static final VeilResourceManagerImpl RESOURCE_MANAGER = new VeilResourceManagerImpl();

    public static void init() {
        VeilRenderSystem.bootstrap();
        QuasarParticleHandler.init();
        QuasarParticles.bootstrap();
        FlareManager.bootstrap();

        VeilEventPlatform.INSTANCE.onFreeNativeResources(() -> {
            VeilRenderSystem.close();
            RESOURCE_MANAGER.free();
        });
        VeilEventPlatform.INSTANCE.onVeilRendererAvailable(renderer -> {
            RESOURCE_MANAGER.addVeilLoaders(renderer);
            RenderStyleRegistry.initRenderStyles();
//            glEnable(GL_DEPTH_CLAMP); // TODO add config option
        });

        if (Veil.IMGUIMC) {
            VeilImGuiCompat.load();
        }

        // This fixes moving transparent blocks drawing too early
        VeilEventPlatform.INSTANCE.onVeilRegisterFixedBuffers(registry -> registry.registerFixedBuffer(VeilRenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS, RenderType.translucentMovingBlock()));

        RenderTypeShardRegistry.addGenericShard(renderType -> "main_target".equals(getOutputName(renderType)), new DynamicBufferShard(DynamicBufferManager.MAIN_WRAPPER, () -> Minecraft.getInstance().getMainRenderTarget()));
        RenderTypeShardRegistry.addGenericShard(renderType -> "translucent_target".equals(getOutputName(renderType)), new DynamicBufferShard("translucent", () -> Minecraft.getInstance().levelRenderer.getTranslucentTarget()));
        RenderTypeShardRegistry.addGenericShard(renderType -> "particles_target".equals(getOutputName(renderType)), new DynamicBufferShard("particles", () -> Minecraft.getInstance().levelRenderer.getParticlesTarget()));
        RenderTypeShardRegistry.addGenericShard(renderType -> "weather_target".equals(getOutputName(renderType)), new DynamicBufferShard("weather", () -> Minecraft.getInstance().levelRenderer.getWeatherTarget()));
        RenderTypeShardRegistry.addGenericShard(renderType -> "clouds_target".equals(getOutputName(renderType)), new DynamicBufferShard("clouds", () -> Minecraft.getInstance().levelRenderer.getCloudsTarget()));
        RenderTypeShardRegistry.addGenericShard(renderType -> "item_entity_target".equals(getOutputName(renderType)), new DynamicBufferShard("item_entity", () -> Minecraft.getInstance().levelRenderer.getItemEntityTarget()));

        PostPipelineStageRegistry.bootstrap();
        LightTypeRegistry.bootstrap();
        RenderTypeLayerRegistry.bootstrap();
        VeilShaderBufferRegistry.bootstrap();
        VeilResourceEditorRegistry.bootstrap();
        EmitterShapeRegistry.bootstrap();
        RenderStyleRegistry.bootstrap();
        ParticleModuleTypeRegistry.bootstrap();
        PropertyRegistry.bootstrap();
        PropertyModifierRegistry.bootstrap();
    }

    private static String getOutputName(RenderType.CompositeRenderType renderType) {
        return VeilRenderType.getName(VeilRenderType.getShards(renderType).outputState());
    }

    public static VeilClientPlatform clientPlatform() {
        return PLATFORM;
    }

    public static VeilResourceManagerImpl resourceManager() {
        return RESOURCE_MANAGER;
    }
}
