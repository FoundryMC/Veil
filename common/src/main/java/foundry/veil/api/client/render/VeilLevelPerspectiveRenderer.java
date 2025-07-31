package foundry.veil.api.client.render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.compat.FlashbackCompat;
import foundry.veil.api.compat.SodiumCompat;
import foundry.veil.ext.RenderTargetExtension;
import foundry.veil.impl.client.render.perspective.FlashbackAccess;
import foundry.veil.impl.client.render.perspective.IrisPipelineAccess;
import foundry.veil.impl.client.render.perspective.LevelPerspectiveCamera;
import foundry.veil.mixin.perspective.accessor.GameRendererAccessor;
import foundry.veil.mixin.perspective.accessor.LevelRendererAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Renders the level from different perspectives.
 *
 * @author Ocelot
 */
public final class VeilLevelPerspectiveRenderer {

    private static final LevelPerspectiveCamera CAMERA = new LevelPerspectiveCamera();
    private static final Matrix4f TRANSFORM = new Matrix4f();
    private static final AtomicInteger ID = new AtomicInteger();

    private static final CameraMatrices BACKUP_CAMERA_MATRICES = new CameraMatrices();
    private static final Matrix4f BACKUP_PROJECTION = new Matrix4f();
    private static final Vector3f BACKUP_LIGHT0_POSITION = new Vector3f();
    private static final Vector3f BACKUP_LIGHT1_POSITION = new Vector3f();

    private static final Matrix4f BACKUP_FLASHBACK_PROJECTION = new Matrix4f();
    private static final Quaternionf BACKUP_FLASHBACK_CAMERA = new Quaternionf();

    private static boolean renderingPerspective = false;

    private VeilLevelPerspectiveRenderer() {
    }

    /**
     * Renders the level from another POV. Automatically prevents circular render references.
     *
     * @param framebuffer       The framebuffer to draw into
     * @param modelView         The base modelview matrix
     * @param projection        The projection matrix
     * @param cameraPosition    The position of the camera
     * @param cameraOrientation The orientation of the camera
     * @param renderDistance    The chunk render distance
     * @param deltaTracker      The delta tracker instance
     * @param drawLights        Whether to draw lights to the scene after
     * @return The full framebuffer including dynamic buffers. This framebuffer is owned by the render system
     */
    public static AdvancedFbo render(AdvancedFbo framebuffer, Matrix4fc modelView, Matrix4fc projection, Vector3dc cameraPosition, Quaternionfc cameraOrientation, float renderDistance, DeltaTracker deltaTracker, boolean drawLights) {
        return render(framebuffer, Minecraft.getInstance().cameraEntity, modelView, projection, cameraPosition, cameraOrientation, renderDistance, deltaTracker, drawLights);
    }

    /**
     * Renders the level from another POV. Automatically prevents circular render references.
     *
     * @param framebuffer       The framebuffer to draw into
     * @param cameraEntity      The entity to draw the camera in relation to. If unsure use {@link #render(AdvancedFbo, Matrix4fc, Matrix4fc, Vector3dc, Quaternionfc, float, DeltaTracker, boolean)}
     * @param modelView         The base modelview matrix
     * @param projection        The projection matrix
     * @param cameraPosition    The position of the camera
     * @param cameraOrientation The orientation of the camera
     * @param renderDistance    The chunk render distance
     * @param deltaTracker      The delta tracker instance
     * @param drawLights        Whether to draw lights to the scene after
     * @return The full framebuffer including dynamic buffers. This framebuffer is owned by the render system
     */
    public static AdvancedFbo render(AdvancedFbo framebuffer, @Nullable Entity cameraEntity, Matrix4fc modelView, Matrix4fc projection, Vector3dc cameraPosition, Quaternionfc cameraOrientation, float renderDistance, DeltaTracker deltaTracker, boolean drawLights) {
        if (renderingPerspective) {
            return framebuffer;
        }

        // Finish anything previously being rendered for safety
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        bufferSource.endBatch();

        final Minecraft minecraft = Minecraft.getInstance();
        final GameRenderer gameRenderer = minecraft.gameRenderer;
        final LevelRenderer levelRenderer = minecraft.levelRenderer;
        final LevelRendererAccessor levelRendererAccessor = (LevelRendererAccessor) levelRenderer;
        final Window window = minecraft.getWindow();
        final GameRendererAccessor accessor = (GameRendererAccessor) gameRenderer;
        final RenderTargetExtension renderTargetExtension = (RenderTargetExtension) minecraft.getMainRenderTarget();
        final PoseStack poseStack = new PoseStack();

        CAMERA.setup(cameraPosition, cameraEntity, minecraft.level, cameraOrientation, renderDistance);

        poseStack.mulPose(TRANSFORM.set(modelView));
        poseStack.mulPose(CAMERA.rotation());

        float backupRenderDistance = gameRenderer.getRenderDistance();
        accessor.setRenderDistance(renderDistance * 16.0F);

        float backupFogStart = RenderSystem.getShaderFogStart();
        float backupFogEnd = RenderSystem.getShaderFogEnd();
        FogShape backupFogShape = RenderSystem.getShaderFogShape();

        int backupWidth = window.getWidth();
        int backupHeight = window.getHeight();
        if (!FlashbackCompat.isLoaded()) {
            window.setWidth(framebuffer.getWidth());
            window.setHeight(framebuffer.getHeight());
        } else {
            FlashbackAccess.backup(BACKUP_FLASHBACK_PROJECTION, BACKUP_FLASHBACK_CAMERA);
        }

        final Object backupPipeline = IrisPipelineAccess.getPipeline(levelRenderer);

        final Object backupRenderLists;
        final Object backupTaskLists;
        if (SodiumCompat.isLoaded()) {
            backupRenderLists = SodiumCompat.INSTANCE.getSortedRenderLists();
            backupTaskLists = SodiumCompat.INSTANCE.getTaskLists();
            ID.getAndIncrement();
        } else {
            backupRenderLists = null;
            backupTaskLists = null;
        }

        BACKUP_PROJECTION.set(RenderSystem.getProjectionMatrix());
        gameRenderer.resetProjectionMatrix(TRANSFORM.set(projection));
        BACKUP_LIGHT0_POSITION.set(VeilRenderSystem.getLight0Direction());
        BACKUP_LIGHT1_POSITION.set(VeilRenderSystem.getLight1Direction());

        Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
        matrix4fstack.pushMatrix();
        matrix4fstack.identity();
        RenderSystem.applyModelViewMatrix();

        HitResult backupHitResult = minecraft.hitResult;
        Entity backupCrosshairPickEntity = minecraft.crosshairPickEntity;

        renderingPerspective = true;
        AdvancedFbo drawFbo = VeilRenderSystem.renderer().getDynamicBufferManger().getDynamicFbo(framebuffer);
        drawFbo.bind(true);
        renderTargetExtension.veil$setWrapper(drawFbo);

        Frustum backupFrustum = levelRendererAccessor.getCullingFrustum();

        CameraMatrices matrices = VeilRenderSystem.renderer().getCameraMatrices();
        matrices.backup(BACKUP_CAMERA_MATRICES);

        try {
            levelRenderer.prepareCullFrustum(new Vec3(cameraPosition.x(), cameraPosition.y(), cameraPosition.z()), poseStack.last().pose(), TRANSFORM);
            levelRenderer.renderLevel(deltaTracker, false, CAMERA, gameRenderer, gameRenderer.lightTexture(), poseStack.last().pose(), TRANSFORM);
            // Make sure all buffers have been finished
            bufferSource.endBatch();
            levelRenderer.doEntityOutline();

            // Draw lights
            if (drawLights) {
                ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
                if (VeilRenderSystem.drawLights(profiler, VeilRenderSystem.getCullingFrustum())) {
                    VeilRenderSystem.compositeLights(profiler);
                } else {
                    AdvancedFbo.unbind();
                }
            }
        } finally {
            matrices.restore(BACKUP_CAMERA_MATRICES);

            levelRendererAccessor.setCullingFrustum(backupFrustum);

            renderTargetExtension.veil$setWrapper(null);
            AdvancedFbo.unbind();
            renderingPerspective = false;

            minecraft.crosshairPickEntity = backupCrosshairPickEntity;
            minecraft.hitResult = backupHitResult;

            matrix4fstack.popMatrix();
            RenderSystem.applyModelViewMatrix();

            RenderSystem.setShaderLights(BACKUP_LIGHT0_POSITION, BACKUP_LIGHT1_POSITION);
            gameRenderer.resetProjectionMatrix(BACKUP_PROJECTION);

            IrisPipelineAccess.setPipeline(levelRenderer, backupPipeline);

            if (SodiumCompat.isLoaded()) {
                SodiumCompat.INSTANCE.setSortedRenderLists(backupRenderLists);
                SodiumCompat.INSTANCE.setTaskList(backupTaskLists);
            }

            RenderSystem.setShaderFogStart(backupFogStart);
            RenderSystem.setShaderFogEnd(backupFogEnd);
            RenderSystem.setShaderFogShape(backupFogShape);

            if (!FlashbackCompat.isLoaded()) {
                window.setWidth(backupWidth);
                window.setHeight(backupHeight);
            } else {
                FlashbackAccess.restore(BACKUP_FLASHBACK_PROJECTION, BACKUP_FLASHBACK_CAMERA);
            }

            accessor.setRenderDistance(backupRenderDistance);

            // Reset the renderers to what they used to be
            Camera mainCamera = gameRenderer.getMainCamera();
            minecraft.getBlockEntityRenderDispatcher().prepare(minecraft.level, mainCamera, minecraft.hitResult);
            minecraft.getEntityRenderDispatcher().prepare(minecraft.level, mainCamera, minecraft.crosshairPickEntity);
        }
        return drawFbo;
    }

    /**
     * @return Whether a perspective is being rendered
     */
    public static boolean isRenderingPerspective() {
        return renderingPerspective;
    }

    @ApiStatus.Internal
    public static int getID() {
        return ID.get();
    }
}
