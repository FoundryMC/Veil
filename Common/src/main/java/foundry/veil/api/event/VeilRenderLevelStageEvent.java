package foundry.veil.api.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;

import java.util.Locale;

/**
 * Fired for each render stage to draw arbitrarily to the screen. This is available as a last-resort if {@link VeilRegisterFixedBuffersEvent} doesn't fit the use case.
 *
 * @author Ocelot
 * @see VeilRegisterFixedBuffersEvent
 */
@FunctionalInterface
public interface VeilRenderLevelStageEvent {

    /**
     * Called when the specified level stage is rendered. This functions the same as Forge.
     *
     * @param stage            The stage rendering
     * @param levelRenderer    The level renderer instance
     * @param bufferSource     The buffer source instance
     * @param poseStack        The current stack of matrix transformations
     * @param projectionMatrix The current projection matrix being used to render
     * @param renderTick       The current tick of rendering
     * @param partialTicks     The percentage from last tick to this tick
     * @param camera           The camera the level is rendered from
     * @param frustum          The view frustum instance
     */
    void onRenderLevelStage(Stage stage, LevelRenderer levelRenderer, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, Matrix4f projectionMatrix, int renderTick, float partialTicks, Camera camera, Frustum frustum);

    /**
     * Stages for rendering specific render types.
     *
     * @author Ocelot
     */
    enum Stage {
        AFTER_SKY,
        AFTER_SOLID_BLOCKS,
        AFTER_CUTOUT_MIPPED_BLOCKS,
        AFTER_CUTOUT_BLOCKS,
        AFTER_ENTITIES,
        AFTER_BLOCK_ENTITIES,
        AFTER_TRANSLUCENT_BLOCKS,
        AFTER_TRIPWIRE_BLOCKS,
        AFTER_PARTICLES,
        AFTER_WEATHER,
        AFTER_LEVEL;

        private final String name;

        Stage() {
            this.name = this.name().toLowerCase(Locale.ROOT);
        }

        /**
         * @return The name of this render stage
         */
        public String getName() {
            return this.name;
        }
    }
}
