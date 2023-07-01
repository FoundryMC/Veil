package foundry.veil.pipeline;

import foundry.veil.render.CameraMatrices;
import foundry.veil.render.framebuffer.FramebufferManager;
import foundry.veil.render.post.PostProcessingManager;
import foundry.veil.render.shader.ShaderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class VeilRendererImpl implements VeilRenderer, NativeResource {

    private final ShaderManager shaderManager;
    private final FramebufferManager framebufferManager;
    private final PostProcessingManager postProcessingManager;
    private final CameraMatrices cameraMatrices;

    public VeilRendererImpl(ReloadableResourceManager resourceManager, TextureManager textureManager) {
        this.shaderManager = new ShaderManager();
        resourceManager.registerReloadListener(this.shaderManager);
        this.framebufferManager = new FramebufferManager();
        resourceManager.registerReloadListener(this.framebufferManager);
        this.postProcessingManager = new PostProcessingManager(this.framebufferManager, textureManager, this.shaderManager);
        resourceManager.registerReloadListener(this.postProcessingManager);
        this.cameraMatrices = new CameraMatrices();
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
    public CameraMatrices getCameraMatrices() {
        return this.cameraMatrices;
    }

    @Override
    public void free() {
        this.shaderManager.close();
        this.framebufferManager.free();
        this.postProcessingManager.free();
        this.cameraMatrices.free();
    }
}
