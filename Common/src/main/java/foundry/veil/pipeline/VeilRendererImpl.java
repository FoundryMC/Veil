package foundry.veil.pipeline;

import foundry.veil.framebuffer.FramebufferManager;
import foundry.veil.post.PostProcessingManager;
import foundry.veil.shader.ShaderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

@ApiStatus.Internal
public class VeilRendererImpl implements VeilRenderer, NativeResource {

    private final ShaderManager shaderManager;
    private final FramebufferManager framebufferManager;
    private final PostProcessingManager postProcessingManager;

    public VeilRendererImpl(ReloadableResourceManager resourceManager, TextureManager textureManager) {
        this.shaderManager = new ShaderManager();
        resourceManager.registerReloadListener(this.shaderManager);
        this.framebufferManager = new FramebufferManager();
        resourceManager.registerReloadListener(this.framebufferManager);
        this.postProcessingManager = new PostProcessingManager(this.framebufferManager, textureManager, this.shaderManager);
        resourceManager.registerReloadListener(this.postProcessingManager);
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
    public PostProcessingManager getPostProcessingManager() {
        return this.postProcessingManager;
    }

    @Override
    public void free() {
        this.framebufferManager.free();
    }
}
