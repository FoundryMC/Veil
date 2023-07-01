package foundry.veil.render.post;

import foundry.veil.render.framebuffer.FramebufferManager;
import foundry.veil.render.shader.ShaderManager;
import net.minecraft.client.renderer.texture.TextureManager;

/**
 * Basic context that most rendering applications need.
 *
 * @author Ocelot
 */
public interface RenderContext {

    /**
     * Draws a quad onto the full screen.
     */
    void drawScreenQuad();

    /**
     * @return The manager for all textures
     */
    TextureManager getTextureManager();

    /**
     * @return The manager for all shaders
     */
    ShaderManager getShaderManager();

    /**
     * @return The manager for all framebuffers
     */
    FramebufferManager getFramebufferManager();
}
