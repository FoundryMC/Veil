package foundry.veil.pipeline;

import foundry.veil.framebuffer.FramebufferManager;
import foundry.veil.post.PostProcessingManager;
import foundry.veil.shader.ShaderManager;

/**
 * Manages the render pipeline for Veil.
 *
 * @author Ocelot
 */
public interface VeilRenderer {

    ShaderManager getShaderManager();

    FramebufferManager getFramebufferManager();

    PostProcessingManager getPostProcessingManager();
}
