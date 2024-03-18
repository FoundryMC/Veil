package foundry.veil.neoforge.platform;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.event.*;
import foundry.veil.neoforge.event.NeoForgeFreeNativeResourcesEvent;
import foundry.veil.neoforge.event.NeoForgeVeilPostProcessingEvent;
import foundry.veil.neoforge.event.NeoForgeVeilRegisterFixedBuffersEvent;
import foundry.veil.neoforge.event.NeoForgeVeilRendererEvent;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

import java.util.Map;

@ApiStatus.Internal
public class NeoForgeVeilEventPlatform implements VeilEventPlatform {

    private static final BiMap<VeilRenderLevelStageEvent.Stage, RenderLevelStageEvent.Stage> STAGE_MAPPING = HashBiMap.create(Map.ofEntries(
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_SKY, RenderLevelStageEvent.Stage.AFTER_SKY),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS, RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS, RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS, RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_ENTITIES, RenderLevelStageEvent.Stage.AFTER_ENTITIES),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES, RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS, RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS, RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_PARTICLES, RenderLevelStageEvent.Stage.AFTER_PARTICLES),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_WEATHER, RenderLevelStageEvent.Stage.AFTER_WEATHER),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_LEVEL, RenderLevelStageEvent.Stage.AFTER_LEVEL)
    ));

    @Override
    public void onFreeNativeResources(FreeNativeResourcesEvent event) {
        NeoForge.EVENT_BUS.addListener(NeoForgeFreeNativeResourcesEvent.class, forgeEvent -> event.onFree());
    }

    @Override
    public void onVeilRendererAvailable(VeilRendererEvent event) {
        NeoForge.EVENT_BUS.addListener(NeoForgeVeilRendererEvent.class, forgeEvent -> event.onVeilRendererAvailable(forgeEvent.getRenderer()));
    }

    @Override
    public void preVeilPostProcessing(VeilPostProcessingEvent.Pre event) {
        NeoForge.EVENT_BUS.addListener(NeoForgeVeilPostProcessingEvent.Pre.class, forgeEvent -> event.preVeilPostProcessing(forgeEvent.getName(), forgeEvent.getPipeline(), forgeEvent.getContext()));
    }

    @Override
    public void postVeilPostProcessing(VeilPostProcessingEvent.Post event) {
        NeoForge.EVENT_BUS.addListener(NeoForgeVeilPostProcessingEvent.Post.class, forgeEvent -> event.postVeilPostProcessing(forgeEvent.getName(), forgeEvent.getPipeline(), forgeEvent.getContext()));
    }

    @Override
    public void onVeilRegisterFixedBuffers(VeilRegisterFixedBuffersEvent event) {
        NeoForge.EVENT_BUS.<NeoForgeVeilRegisterFixedBuffersEvent>addListener(forgeEvent -> event.onRegisterFixedBuffers((stage, renderType) -> {
            if (stage == null) {
                forgeEvent.register(null, renderType);
                return;
            }

            RenderLevelStageEvent.Stage forgeStage = STAGE_MAPPING.get(stage);
            if (forgeStage != null) {
                forgeEvent.register(forgeStage, renderType);
            }
        }));
    }

    @Override
    public void onVeilRenderTypeStageRender(VeilRenderLevelStageEvent event) {
        NeoForge.EVENT_BUS.<RenderLevelStageEvent>addListener(forgeEvent -> {
            VeilRenderLevelStageEvent.Stage stage = STAGE_MAPPING.inverse().get(forgeEvent.getStage());
            if (stage == null) {
                return;
            }

            LevelRenderer levelRenderer = forgeEvent.getLevelRenderer();
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            PoseStack poseStack = forgeEvent.getPoseStack();
            Matrix4f projectionMatrix = forgeEvent.getProjectionMatrix();
            int renderTick = forgeEvent.getRenderTick();
            float partialTicks = forgeEvent.getPartialTick();
            Camera camera = forgeEvent.getCamera();
            Frustum frustum = forgeEvent.getFrustum();
            event.onRenderLevelStage(stage, levelRenderer, bufferSource, poseStack, projectionMatrix, renderTick, partialTicks, camera, frustum);
        });
    }
}
