package foundry.veil.post;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import foundry.veil.framebuffer.AdvancedFbo;
import foundry.veil.framebuffer.FramebufferManager;
import foundry.veil.framebuffer.VeilFramebuffers;
import foundry.veil.shader.ShaderManager;
import foundry.veil.shader.ShaderProgram;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link PostPipeline.Context}.
 */
public class PostPipelineContext implements PostPipeline.Context, NativeResource {

    private final FramebufferManager framebufferManager;
    private final TextureManager textureManager;
    private final ShaderManager shaderManager;
    private final VertexBuffer vbo;
    private final Map<CharSequence, Integer> samplers;
    private final Map<ResourceLocation, AdvancedFbo> framebuffers;

    /**
     * Creates a new context to fit the specified window.
     *
     * @param framebufferManager The manager for all custom framebuffers
     * @param textureManager     The texture manager instance
     * @param shaderManager      The shader manager instance
     */
    public PostPipelineContext(FramebufferManager framebufferManager, TextureManager textureManager, ShaderManager shaderManager) {
        this.framebufferManager = framebufferManager;
        this.textureManager = textureManager;
        this.shaderManager = shaderManager;
        this.vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.samplers = new HashMap<>();
        this.framebuffers = new HashMap<>();
        this.setupScreenQuad();
    }

    private void setupScreenQuad() {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(-1, 1, 0).endVertex();
        bufferBuilder.vertex(-1, -1, 0).endVertex();
        bufferBuilder.vertex(1, 1, 0).endVertex();
        bufferBuilder.vertex(1, -1, 0).endVertex();

        this.vbo.bind();
        this.vbo.upload(bufferBuilder.end());
        VertexBuffer.unbind();
    }

    /**
     * Marks the start of a new post run.
     */
    public void begin() {
        this.framebufferManager.getFramebuffers().forEach(this::setFramebuffer);
        this.setFramebuffer(VeilFramebuffers.MAIN, this.getDrawFramebuffer());
    }

    /**
     * Ends the running pass and cleans up resources.
     */
    public void end() {
        this.samplers.clear();
        this.framebuffers.clear();
    }

    @Override
    public void drawScreenQuad() {
        this.vbo.bind();
        this.vbo.draw();
        VertexBuffer.unbind();
    }

    @Override
    public void setSampler(CharSequence name, int id) {
        this.samplers.put(name, id);
    }

    @Override
    public void setFramebuffer(ResourceLocation name, AdvancedFbo framebuffer) {
        this.framebuffers.put(name, framebuffer);
    }

    @Override
    public void applySamplers(ShaderProgram shader) {
        this.samplers.forEach(shader::addSampler);
    }

    @Override
    public @Nullable AdvancedFbo getFramebuffer(ResourceLocation name) {
        return this.framebuffers.get(name);
    }

    @Override
    public AdvancedFbo getDrawFramebuffer() {
        return this.framebuffers.getOrDefault(VeilFramebuffers.POST, AdvancedFbo.getMainFramebuffer());
    }

    @Override
    public TextureManager getTextureManager() {
        return this.textureManager;
    }

    @Override
    public ShaderManager getShaderManager() {
        return this.shaderManager;
    }

    @Override
    public FramebufferManager getFramebufferManager() {
        return this.framebufferManager;
    }

    @Override
    public void free() {
        this.vbo.close();
    }
}
