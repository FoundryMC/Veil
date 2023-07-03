package foundry.veil.render.pipeline;

import foundry.veil.render.CameraMatrices;
import foundry.veil.render.GuiInfo;
import foundry.veil.render.framebuffer.FramebufferManager;
import foundry.veil.render.post.PostPipeline;
import foundry.veil.render.post.PostProcessingManager;
import foundry.veil.render.shader.ShaderManager;
import foundry.veil.render.shader.ShaderModificationManager;
import foundry.veil.render.shader.definition.ShaderPreDefinitions;

/**
 * Manages the render pipeline for Veil.
 *
 * @author Ocelot
 */
public interface VeilRenderer {

    /**
     * @return The set of shader pre-definitions. Changes are automatically synced the next frame
     */
    default ShaderPreDefinitions getDefinitions() {
        return this.getShaderManager().getDefinitions();
    }

    /**
     * @return The manager for all custom shader modifications
     */
    ShaderModificationManager getShaderModificationManager();

    /**
     * @return The manager for all veil shaders
     */
    ShaderManager getShaderManager();

    /**
     * @return The manager for all custom veil framebuffers
     */
    FramebufferManager getFramebufferManager();

    /**
     * @return The manager for all {@link PostPipeline} instances
     */
    PostProcessingManager getPostProcessingManager();

    /**
     * @return The camera matrices instance
     */
    CameraMatrices getCameraMatrices();

    /**
     * @return The gui info instance
     */
    GuiInfo getGuiInfo();
}
