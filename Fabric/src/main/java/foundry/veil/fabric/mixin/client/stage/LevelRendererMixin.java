package foundry.veil.fabric.mixin.client.stage;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.fabric.FabricRenderTypeStageHandler;
import foundry.veil.fabric.ext.LevelRendererExtension;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin implements LevelRendererExtension {

    @Shadow
    private @Nullable ClientLevel level;

    @Shadow
    private int ticks;

    @Shadow
    private @Nullable Frustum capturedFrustum;

    @Shadow
    private Frustum cullingFrustum;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Unique
    private float veil$capturePartialTicks;
    @Unique
    private Camera veil$captureCamera;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    public void capture(PoseStack poseStack, float partialTicks, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo ci) {
        this.veil$capturePartialTicks = partialTicks;
        this.veil$captureCamera = camera;
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    public void clear(PoseStack poseStack, float partialTicks, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo ci) {
        this.veil$captureCamera = null;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void postRenderSky(PoseStack poseStack, float partialTicks, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo ci, TickRateManager tickRateManager, float f, ProfilerFiller profiler, Vec3 cameraPos, double x, double y, double z, Matrix4f matrix4f2, boolean flag, Frustum frustum) {
        FabricRenderTypeStageHandler.renderStage(profiler, VeilRenderLevelStageEvent.Stage.AFTER_SKY, (LevelRenderer) (Object) this, this.renderBuffers.bufferSource(), poseStack, projection, this.ticks, partialTicks, camera, frustum);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V", ordinal = 3, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void postRenderEntities(PoseStack poseStack, float partialTicks, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo ci, TickRateManager tickRateManager, float f, ProfilerFiller profiler, Vec3 cameraPos, double x, double y, double z, Matrix4f matrix4f2, boolean flag, Frustum frustum) {
        FabricRenderTypeStageHandler.renderStage(profiler, VeilRenderLevelStageEvent.Stage.AFTER_ENTITIES, (LevelRenderer) (Object) this, this.renderBuffers.bufferSource(), poseStack, projection, this.ticks, partialTicks, camera, frustum);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;long2ObjectEntrySet()Lit/unimi/dsi/fastutil/objects/ObjectSet;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void postRenderBlockEntities(PoseStack poseStack, float partialTicks, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo ci, TickRateManager tickRateManager, float f, ProfilerFiller profiler, Vec3 cameraPos, double x, double y, double z, Matrix4f matrix4f2, boolean flag, Frustum frustum) {
        FabricRenderTypeStageHandler.renderStage(profiler, VeilRenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES, (LevelRenderer) (Object) this, this.renderBuffers.bufferSource(), poseStack, projection, this.ticks, partialTicks, camera, frustum);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void postRenderParticles(PoseStack poseStack, float partialTicks, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo ci, TickRateManager tickRateManager, float f, ProfilerFiller profiler, Vec3 cameraPos, double x, double y, double z, Matrix4f matrix4f2, boolean flag, Frustum frustum) {
        FabricRenderTypeStageHandler.renderStage(profiler, VeilRenderLevelStageEvent.Stage.AFTER_PARTICLES, (LevelRenderer) (Object) this, this.renderBuffers.bufferSource(), poseStack, projection, this.ticks, partialTicks, camera, frustum);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void postRenderWeather(PoseStack poseStack, float partialTicks, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo ci, TickRateManager tickRateManager, float f, ProfilerFiller profiler, Vec3 cameraPos, double x, double y, double z, Matrix4f matrix4f2, boolean flag, Frustum frustum) {
        FabricRenderTypeStageHandler.renderStage(profiler, VeilRenderLevelStageEvent.Stage.AFTER_WEATHER, (LevelRenderer) (Object) this, this.renderBuffers.bufferSource(), poseStack, projection, this.ticks, partialTicks, camera, frustum);
    }

    @Inject(method = "renderLevel", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void postRenderLevel(PoseStack poseStack, float partialTicks, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo ci, TickRateManager tickRateManager, float f, ProfilerFiller profiler, Vec3 cameraPos, double x, double y, double z, Matrix4f matrix4f2, boolean flag, Frustum frustum) {
        FabricRenderTypeStageHandler.renderStage(profiler, VeilRenderLevelStageEvent.Stage.AFTER_LEVEL, (LevelRenderer) (Object) this, this.renderBuffers.bufferSource(), poseStack, projection, this.ticks, partialTicks, camera, frustum);
    }

    @Override
    public void veil$renderStage(RenderType layer, PoseStack poseStack, Matrix4f projection) {
        VeilRenderLevelStageEvent.Stage stage;
        if (layer == RenderType.solid()) {
            stage = VeilRenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS;
        } else if (layer == RenderType.cutoutMipped()) {
            stage = VeilRenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS;
        } else if (layer == RenderType.cutout()) {
            stage = VeilRenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS;
        } else if (layer == RenderType.translucent()) {
            stage = VeilRenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS;
        } else if (layer == RenderType.tripwire()) {
            stage = VeilRenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS;
        } else {
            stage = null;
        }

        if (stage != null) {
            FabricRenderTypeStageHandler.renderStage(this.level.getProfiler(), stage, (LevelRenderer) (Object) this, this.renderBuffers.bufferSource(), poseStack, projection, this.ticks, this.veil$capturePartialTicks, this.veil$captureCamera, this.capturedFrustum != null ? this.capturedFrustum : this.cullingFrustum);
        }
    }
}
