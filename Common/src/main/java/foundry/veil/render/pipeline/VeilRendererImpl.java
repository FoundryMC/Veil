package foundry.veil.render.pipeline;

import foundry.veil.editor.EditorManager;
import foundry.veil.mixin.client.pipeline.ReloadableResourceManagerAccessor;
import foundry.veil.render.CameraMatrices;
import foundry.veil.render.GuiInfo;
import foundry.veil.render.framebuffer.FramebufferManager;
import foundry.veil.render.post.PostProcessingManager;
import foundry.veil.render.shader.ShaderManager;
import foundry.veil.render.shader.ShaderModificationManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

import java.util.List;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class VeilRendererImpl implements VeilRenderer, NativeResource {

    private final ShaderModificationManager shaderModificationManager;
    private final ShaderManager shaderManager;
    private final FramebufferManager framebufferManager;
    private final PostProcessingManager postProcessingManager;
    private final EditorManager editorManager;
    private final CameraMatrices cameraMatrices;
    private final GuiInfo guiInfo;

    public VeilRendererImpl(ReloadableResourceManager resourceManager, TextureManager textureManager) {
        this.shaderModificationManager = new ShaderModificationManager();
        this.shaderManager = new ShaderManager(this.shaderModificationManager);
        this.framebufferManager = new FramebufferManager();
        this.postProcessingManager = new PostProcessingManager(this.framebufferManager, textureManager, this.shaderManager);
        this.editorManager = new EditorManager();
        this.cameraMatrices = new CameraMatrices();
        this.guiInfo = new GuiInfo();

        List<PreparableReloadListener> listeners = ((ReloadableResourceManagerAccessor) resourceManager).getListeners();

        // This must finish loading before the game renderer so modifications can apply on load
        listeners.add(0, this.shaderModificationManager);
        // This must be before vanilla shaders so vanilla shaders can be replaced
        listeners.add(1, this.shaderManager);
        resourceManager.registerReloadListener(this.framebufferManager);
        resourceManager.registerReloadListener(this.postProcessingManager);
    }

    @Override
    public ShaderModificationManager getShaderModificationManager() {
        return this.shaderModificationManager;
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
    public EditorManager getEditorManager() {
        return this.editorManager;
    }

    @Override
    public CameraMatrices getCameraMatrices() {
        return this.cameraMatrices;
    }

    @Override
    public GuiInfo getGuiInfo() {
        return this.guiInfo;
    }

    @Override
    public void free() {
        this.shaderManager.close();
        this.framebufferManager.free();
        this.postProcessingManager.free();
        this.cameraMatrices.free();
        this.guiInfo.free();
    }
}
