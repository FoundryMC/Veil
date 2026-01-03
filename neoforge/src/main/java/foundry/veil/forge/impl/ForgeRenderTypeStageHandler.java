package foundry.veil.forge.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.ext.LevelRendererBlockLayerExtension;
import foundry.veil.forge.platform.NeoForgeVeilEventPlatform;
import foundry.veil.mixin.rendertype.accessor.RenderTypeBufferSourceAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@ApiStatus.Internal
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = Veil.MODID, value = Dist.CLIENT)
public class ForgeRenderTypeStageHandler {

    private static final Map<RenderLevelStageEvent.Stage, Set<RenderType>> STAGE_RENDER_TYPES = new HashMap<>();
    private static Set<RenderType> CUSTOM_BLOCK_LAYERS = Set.of();
    private static List<RenderType> BLOCK_LAYERS;

    public static synchronized void register(@Nullable RenderLevelStageEvent.Stage stage, RenderType renderType) {
        SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers = ((RenderTypeBufferSourceAccessor) Minecraft.getInstance().renderBuffers().bufferSource()).getFixedBuffers();
        ByteBufferBuilder old = fixedBuffers.put(renderType, new ByteBufferBuilder(renderType.bufferSize()));
        if (old != null) {
            old.close();
        }

        if (stage != null) {
            STAGE_RENDER_TYPES.computeIfAbsent(stage, unused -> new ObjectArraySet<>()).add(renderType);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLevelStageEnd(RenderLevelStageEvent event) {
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        RenderLevelStageEvent.Stage stage = event.getStage();

        Set<RenderType> stages = STAGE_RENDER_TYPES.get(stage);
        if (stages != null) {
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            stages.forEach(renderType -> {
                profiler.push("render_" + VeilRenderType.getName(renderType));
                if (CUSTOM_BLOCK_LAYERS.contains(renderType)) {
                    Vec3 pos = event.getCamera().getPosition();
                    ((LevelRendererBlockLayerExtension) event.getLevelRenderer()).veil$drawBlockLayer(renderType, pos.x, pos.y, pos.z, event.getModelViewMatrix(), event.getProjectionMatrix());
                }
                bufferSource.endBatch(renderType);
                profiler.pop();
            });
        }

        if (!VeilLevelPerspectiveRenderer.isRenderingPerspective()) {
            VeilRenderLevelStageEvent.Stage veilStage = NeoForgeVeilEventPlatform.getVeilStage(stage);
            if (veilStage != null) {
                profiler.push("post");
                VeilRenderSystem.renderPost(veilStage);
                profiler.pop();
            }
        }
    }

    // Some mods add custom block layers by changing the field, so account for that
    public static List<RenderType> getBlockLayers(List<RenderType> base) {
        if (CUSTOM_BLOCK_LAYERS.isEmpty()) {
            return base;
        }

        if (BLOCK_LAYERS == null || base.size() != BLOCK_LAYERS.size()) {
            ImmutableList.Builder<RenderType> blockLayers = ImmutableList.builder();
            blockLayers.addAll(base);
            if (CUSTOM_BLOCK_LAYERS != null) {
                blockLayers.addAll(CUSTOM_BLOCK_LAYERS);

                // Assign NeoForge chunk layer ids
                int i = base.getLast().chunkLayerId;
                for (RenderType blockLayer : CUSTOM_BLOCK_LAYERS) {
                    blockLayer.chunkLayerId = ++i;
                }
            }
            BLOCK_LAYERS = blockLayers.build();
        }
        return BLOCK_LAYERS;
    }

    public static void setBlockLayers(Set<RenderType> blockLayers) {
        CUSTOM_BLOCK_LAYERS = Set.copyOf(blockLayers);
        BLOCK_LAYERS = null;
    }
}
