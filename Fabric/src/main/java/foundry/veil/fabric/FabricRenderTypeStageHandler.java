package foundry.veil.fabric;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.fabric.event.FabricVeilRenderLevelStageEvent;
import foundry.veil.mixin.client.deferred.RenderBuffersAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

import java.util.*;

@ApiStatus.Internal
public class FabricRenderTypeStageHandler {

    private static final Map<VeilRenderLevelStageEvent.Stage, Set<RenderType>> STAGE_RENDER_TYPES = new HashMap<>();

    public static void register(VeilRenderLevelStageEvent.Stage stage, RenderType renderType) {
        SortedMap<RenderType, BufferBuilder> fixedBuffers = ((RenderBuffersAccessor) Minecraft.getInstance().renderBuffers()).getFixedBuffers();
        fixedBuffers.put(renderType, new BufferBuilder(renderType.bufferSize()));

        STAGE_RENDER_TYPES.computeIfAbsent(stage, unused -> new HashSet<>()).add(renderType);
    }

    public static void renderStage(ProfilerFiller profiler, VeilRenderLevelStageEvent.Stage stage, LevelRenderer levelRenderer, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, Matrix4f projectionMatrix, int renderTick, float partialTicks, Camera camera, Frustum frustum) {
        profiler.push(stage.getName());
        FabricVeilRenderLevelStageEvent.EVENT.invoker().onRenderLevelStage(stage, levelRenderer, bufferSource, poseStack, projectionMatrix, renderTick, partialTicks, camera, frustum);
        profiler.pop();

        Set<RenderType> stages = STAGE_RENDER_TYPES.get(stage);
        if (stages != null) {
            stages.forEach(bufferSource::endBatch);
        }
    }
}
