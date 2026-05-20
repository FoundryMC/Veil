package foundry.veil.fabric.compat.sodium;

import foundry.veil.api.compat.SodiumCompat;
import foundry.veil.fabric.ext.ShaderChunkRendererExtension;
import foundry.veil.fabric.ext.SodiumWorldRendererExtension;
import foundry.veil.fabric.mixin.compat.sodium.RenderSectionManagerAccessor;
import foundry.veil.fabric.mixin.compat.sodium.SodiumWorldRendererAccessor;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.caffeinemc.mods.sodium.client.gl.shader.GlProgram;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.TaskQueueType;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkFogMode;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;

public class VeilFabricSodiumCompat implements SodiumCompat {

    private static @NotNull StringBuilder getShaderName(ChunkShaderOptions options) {
        StringBuilder name = new StringBuilder("chunk_shader");
        if (options.fog() == ChunkFogMode.SMOOTH) {
            name.append("_fog_smooth");
        }

        TerrainRenderPass pass = options.pass();
        if (pass.isTranslucent()) {
            name.append("_translucent");
        }
        if (pass.supportsFragmentDiscard()) {
            name.append("_cutout");
        }
        return name;
    }

    @Override
    public Object2IntMap<ResourceLocation> getLoadedShaders() {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer != null) {
            RenderSectionManagerAccessor renderSectionManager = (RenderSectionManagerAccessor) ((SodiumWorldRendererAccessor) worldRenderer).getRenderSectionManager();
            if (renderSectionManager != null && renderSectionManager.getChunkRenderer() instanceof ShaderChunkRendererExtension extension) {
                Object2IntMap<ResourceLocation> shaders = new Object2IntArrayMap<>(extension.veil$getPrograms().size());

                for (Map.Entry<ChunkShaderOptions, GlProgram<ChunkShaderInterface>> entry : extension.veil$getPrograms().entrySet()) {
                    StringBuilder name = getShaderName(entry.getKey());
                    shaders.put(ResourceLocation.fromNamespaceAndPath("sodium", name.toString()), entry.getValue().handle());
                }
                return shaders;
            }
        }
        return Object2IntMaps.emptyMap();
    }

    @Override
    public void recompile() {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer != null) {
            RenderSectionManagerAccessor renderSectionManager = (RenderSectionManagerAccessor) ((SodiumWorldRendererAccessor) worldRenderer).getRenderSectionManager();
            if (renderSectionManager != null && renderSectionManager.getChunkRenderer() instanceof ShaderChunkRendererExtension extension) {
                extension.veil$recompile();
            }
        }
    }

    @Override
    public void setActiveBuffers(int activeBuffers) {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer != null) {
            RenderSectionManagerAccessor renderSectionManager = (RenderSectionManagerAccessor) ((SodiumWorldRendererAccessor) worldRenderer).getRenderSectionManager();
            if (renderSectionManager != null && renderSectionManager.getChunkRenderer() instanceof ShaderChunkRendererExtension extension) {
                extension.veil$setActiveBuffers(activeBuffers);
            }
        }
    }

    @Override
    public void markChunksDirty() {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer != null) {
            RenderSectionManager renderSectionManager = ((SodiumWorldRendererAccessor) worldRenderer).getRenderSectionManager();

            if (renderSectionManager != null) {
                Long2ReferenceMap<RenderSection> map = ((RenderSectionManagerAccessor) renderSectionManager).getSectionByPosition();
                for (LongIterator iterator = map.keySet().iterator(); iterator.hasNext(); ) {
                    long sectionPos = iterator.nextLong();
                    renderSectionManager.scheduleRebuild(SectionPos.x(sectionPos), SectionPos.y(sectionPos), SectionPos.z(sectionPos), true);
                }
            }
        }
    }

    @Override
    public Object getSortedRenderLists() {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer == null) {
            return SortedRenderLists.empty();
        }
        return ((SodiumWorldRendererExtension) worldRenderer).veil$getSortedRenderLists();
    }

    @Override
    public void setSortedRenderLists(@Nullable Object sortedRenderLists) {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer != null) {
            SortedRenderLists renderLists = sortedRenderLists != null ? (SortedRenderLists) sortedRenderLists : SortedRenderLists.empty();
            ((SodiumWorldRendererExtension) worldRenderer).veil$setSortedRenderLists(renderLists);
        }
    }

    @Override
    public Object getTaskLists() {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer != null) {
            return ((SodiumWorldRendererExtension) worldRenderer).veil$getTaskLists();
        }

        Map<TaskQueueType, ArrayDeque<RenderSection>> taskLists = new EnumMap<>(TaskQueueType.class);
        for (TaskQueueType type : TaskQueueType.values()) {
            taskLists.put(type, new ArrayDeque<>());
        }
        return taskLists;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setTaskList(@Nullable Object taskList) {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer != null) {
            if (taskList != null) {
                ((SodiumWorldRendererExtension) worldRenderer).veil$setTaskLists((Map<TaskQueueType, ArrayDeque<RenderSection>>) taskList);
            } else {
                Map<TaskQueueType, ArrayDeque<RenderSection>> taskLists = new EnumMap<>(TaskQueueType.class);
                for (TaskQueueType type : TaskQueueType.values()) {
                    taskLists.put(type, new ArrayDeque<>());
                }
                ((SodiumWorldRendererExtension) worldRenderer).veil$setTaskLists(taskLists);
            }
        }
    }
}
