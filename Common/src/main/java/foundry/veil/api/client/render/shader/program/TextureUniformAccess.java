package foundry.veil.api.client.render.shader.program;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.AdvancedFboTextureAttachment;
import foundry.veil.api.client.render.shader.texture.ShaderTextureSource;
import org.jetbrains.annotations.Nullable;

/**
 * Provides write access to all textures in a shader program.
 *
 * @author Ocelot
 */
public interface TextureUniformAccess {

    /**
     * Applies the {@link RenderSystem} textures to <code>Sampler0</code>-<code>Sampler11</code>.
     */
    default void addRenderSystemTextures() {
        for (int i = 0; i < 12; ++i) {
            this.addSampler("Sampler" + i, RenderSystem.getShaderTexture(i));
        }
    }

    /**
     * Sets <code>DiffuseSampler0</code>-<code>DiffuseSamplerMax</code>
     * to the color buffers in the specified framebuffer.
     *
     * @param framebuffer The framebuffer to bind samplers from
     */
    default void setFramebufferSamplers(AdvancedFbo framebuffer) {
        int activeTexture = GlStateManager._getActiveTexture();
        for (int i = 0; i < framebuffer.getColorAttachments(); i++) {
            if (!framebuffer.isColorTextureAttachment(i)) {
                continue;
            }

            AdvancedFboTextureAttachment attachment = framebuffer.getColorTextureAttachment(i);
            this.addSampler("DiffuseSampler" + i, attachment.getId());
            if (attachment.getName() != null) {
                this.addSampler(attachment.getName(), attachment.getId());
            }
        }

        if (framebuffer.isDepthTextureAttachment()) {
            AdvancedFboTextureAttachment attachment = framebuffer.getDepthTextureAttachment();
            this.addSampler("DiffuseDepthSampler", framebuffer.getDepthTextureAttachment().getId());
            if (attachment.getName() != null) {
                this.addSampler(attachment.getName(), attachment.getId());
            }
        }

        RenderSystem.activeTexture(activeTexture);
    }

    /**
     * Adds a texture that is dynamically bound and sets texture units.
     *
     * @param name      The name of the texture to set
     * @param textureId The id of the texture to bind and assign a texture unit
     */
    void addSampler(CharSequence name, int textureId);

    /**
     * Removes the specified sampler binding.
     *
     * @param name The name of the sampler to remove
     */
    void removeSampler(CharSequence name);

    /**
     * Loads the samplers set by {@link #addSampler(CharSequence, int)} into the shader.
     *
     * @param sampler The sampler to start binding to
     * @return The next available sampler
     */
    default int applyShaderSamplers(int sampler) {
        return this.applyShaderSamplers(ShaderTextureSource.GLOBAL_CONTEXT, sampler);
    }

    /**
     * Loads the samplers set by {@link #addSampler(CharSequence, int)} into the shader.
     *
     * @param context The context for setting built-in shader samplers or <code>null</code> to ignore normal samplers
     * @param sampler The sampler to start binding to
     * @return The next available sampler
     */
    int applyShaderSamplers(@Nullable ShaderTextureSource.Context context, int sampler);

    /**
     * Clears all samplers.
     */
    void clearSamplers();
}
