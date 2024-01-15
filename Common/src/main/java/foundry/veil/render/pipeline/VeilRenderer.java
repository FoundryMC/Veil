package foundry.veil.render.pipeline;

import foundry.veil.editor.EditorManager;
import foundry.veil.ext.LevelRendererExtension;
import foundry.veil.render.CameraMatrices;
import foundry.veil.render.GuiInfo;
import foundry.veil.render.deferred.VeilDeferredRenderer;
import foundry.veil.render.framebuffer.FramebufferManager;
import foundry.veil.render.post.PostPipeline;
import foundry.veil.render.post.PostProcessingManager;
import foundry.veil.render.shader.ShaderManager;
import foundry.veil.render.shader.ShaderModificationManager;
import foundry.veil.render.shader.definition.ShaderPreDefinitions;
import foundry.veil.render.wrapper.CullFrustum;
import net.minecraft.client.Minecraft;

/**
 * Manages the render pipeline for Veil.
 *
 * @author Ocelot
 */
public interface VeilRenderer {

    /**
     * @return The manager for all custom shader modifications
     */
    ShaderModificationManager getShaderModificationManager();

    /**
     * @return The set of shader pre-definitions. Changes are automatically synced the next frame
     */
    ShaderPreDefinitions getShaderDefinitions();

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
     * @return The deferred renderer instance
     */
    VeilDeferredRenderer getDeferredRenderer();

    /**
     * @return The manager for all editors
     */
    EditorManager getEditorManager();

    /**
     * @return The camera matrices instance
     */
    CameraMatrices getCameraMatrices();

    /**
     * @return The gui info instance
     */
    GuiInfo getGuiInfo();

    /**
     * @return The culling frustum for the renderer
     */
    default CullFrustum getCullingFrustum() {
        return ((LevelRendererExtension) Minecraft.getInstance().levelRenderer).veil$getCullFrustum();
    }
}
