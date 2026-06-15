package foundry.veil.api.client.render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.Veil;
import foundry.veil.api.client.editor.EditorManager;
import foundry.veil.api.client.render.dynamicbuffer.DynamicBufferType;
import foundry.veil.api.client.render.framebuffer.FramebufferManager;
import foundry.veil.api.client.render.light.renderer.LightRenderer;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.ShaderModificationManager;
import foundry.veil.api.client.render.shader.ShaderPreDefinitions;
import foundry.veil.api.flare.FlareEffectManager;
import foundry.veil.api.quasar.particle.ParticleSystemManager;
import foundry.veil.impl.client.render.dynamicbuffer.DynamicBufferManager;
import foundry.veil.impl.client.render.dynamicbuffer.VanillaShaderCompiler;
import foundry.veil.impl.client.render.pipeline.VeilBloomRenderer;
import foundry.veil.impl.client.render.pipeline.VeilFirstPersonRenderer;
import foundry.veil.impl.client.render.rendertype.DynamicRenderTypeManager;
import foundry.veil.impl.client.render.shader.injection.ShaderInjectionManager;
import foundry.veil.mixin.pipeline.accessor.PipelineReloadableResourceManagerAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Manages the render pipeline for Veil.
 *
 * @author Ocelot
 */
public class VeilRenderer implements ResourceManagerReloadListener {

    public static final ResourceLocation ALBEDO_BUFFER_TEXTURE = Veil.veilPath("dynamic_buffer/albedo");
    public static final ResourceLocation NORMAL_BUFFER_TEXTURE = Veil.veilPath("dynamic_buffer/normal");
    public static final ResourceLocation LIGHT_UV_BUFFER_TEXTURE = Veil.veilPath("dynamic_buffer/light_uv");
    public static final ResourceLocation LIGHT_COLOR_BUFFER_TEXTURE = Veil.veilPath("dynamic_buffer/light_color");
    public static final ResourceLocation DEBUG_BUFFER_TEXTURE = Veil.veilPath("dynamic_buffer/debug");

    public static final ResourceLocation COMPOSITE = Veil.veilPath("core/composite");

    private final VanillaShaderCompiler vanillaShaderCompiler;
    private final DynamicBufferManager dynamicBufferManager;
    private final ShaderModificationManager shaderModificationManager;
    private final ShaderInjectionManager shaderInjectionManager;
    private final ShaderPreDefinitions shaderPreDefinitions;
    private final ShaderManager shaderManager;
    private final FramebufferManager framebufferManager;
    private final PostProcessingManager postProcessingManager;
    private final DynamicRenderTypeManager dynamicRenderTypeManager;
    private final ParticleSystemManager quasarParticleManager;
    private final FlareEffectManager flareEffectManager;
    private final EditorManager editorManager;
    private final CameraMatrices cameraMatrices;
    private final LightRenderer lightRenderer;
    private final GuiInfo guiInfo;

    @ApiStatus.Internal
    public VeilRenderer(ReloadableResourceManager resourceManager, Window window) {
        this.vanillaShaderCompiler = new VanillaShaderCompiler();
        this.dynamicBufferManager = new DynamicBufferManager(window.getWidth(), window.getHeight());
        this.shaderPreDefinitions = new ShaderPreDefinitions();
        this.shaderModificationManager = new ShaderModificationManager();
        this.shaderInjectionManager = new ShaderInjectionManager();
        this.shaderManager = new ShaderManager(ShaderManager.PROGRAM_SET, this.shaderPreDefinitions, this.dynamicBufferManager);
        this.framebufferManager = new FramebufferManager();
        this.postProcessingManager = new PostProcessingManager();
        this.dynamicRenderTypeManager = new DynamicRenderTypeManager();
        this.quasarParticleManager = new ParticleSystemManager();
        this.flareEffectManager = new FlareEffectManager();
        this.editorManager = new EditorManager(resourceManager);
        this.cameraMatrices = new CameraMatrices();
        this.lightRenderer = new LightRenderer();
        this.guiInfo = new GuiInfo();

        List<PreparableReloadListener> listeners = ((PipelineReloadableResourceManagerAccessor) resourceManager).getListeners();

        // This must finish loading before the game renderer so modifications can apply on load
        listeners.add(0, this.shaderInjectionManager);
        // This must be before vanilla shaders so vanilla shaders can be replaced
        listeners.add(1, this.shaderManager);
        resourceManager.registerReloadListener(this.framebufferManager);
        resourceManager.registerReloadListener(this.postProcessingManager);
        resourceManager.registerReloadListener(this.dynamicRenderTypeManager);
        resourceManager.registerReloadListener(this.flareEffectManager.getShellManager());
        resourceManager.registerReloadListener(this);
    }

    @ApiStatus.Internal
    public void addDebugInfo(Consumer<String> consumer) {
        consumer.accept("");
        consumer.accept(ChatFormatting.UNDERLINE + "Veil");

        this.lightRenderer.addDebugInfo(consumer);
        int mask = this.dynamicBufferManager.getActiveBuffers();
        if (mask != 0) {
            String buffers = Arrays.stream(DynamicBufferType.decode(mask)).map(DynamicBufferType::getName).collect(Collectors.joining(", "));
            consumer.accept("Active Buffers: " + buffers);
        }
    }

    /**
     * Enables the specified dynamic render buffers.
     *
     * @param name    The name of the "source" of the buffer change
     * @param buffers The buffers to enable
     * @return Whether any change occurred
     */
    public boolean enableBuffers(ResourceLocation name, DynamicBufferType... buffers) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (buffers.length == 0) {
            return false;
        }
        int active = this.dynamicBufferManager.getActiveBuffers(name) | DynamicBufferType.encode(buffers);
        return this.dynamicBufferManager.setActiveBuffers(name, active);
    }

    /**
     * Disables the specified dynamic render buffers.
     *
     * @param name    The name of the "source" of the buffer change
     * @param buffers The buffers to disable
     * @return Whether any change occurred
     */
    public boolean disableBuffers(ResourceLocation name, DynamicBufferType... buffers) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (buffers.length == 0) {
            return false;
        }

        int active = this.dynamicBufferManager.getActiveBuffers(name) & ~DynamicBufferType.encode(buffers);
        return this.dynamicBufferManager.setActiveBuffers(name, active);
    }

    /**
     * Disables the specified dynamic render buffers.
     *
     * @param name The name of the "source" of the buffer change
     * @return Whether any change occurred
     */
    public boolean disableBuffers(ResourceLocation name) {
        RenderSystem.assertOnRenderThreadOrInit();
        return this.dynamicBufferManager.setActiveBuffers(name, 0);
    }

    /**
     * Retrieves the currently active dynamic buffers.
     *
     * @return The currently active buffers
     * @see DynamicBufferType#decode(int)
     * @since 2.3.0
     */
    public int getActiveBuffers() {
        return this.dynamicBufferManager.getActiveBuffers();
    }

    /**
     * @return The Veil compiler for vanilla shaders
     */
    public VanillaShaderCompiler getVanillaShaderCompiler() {
        return this.vanillaShaderCompiler;
    }

    /**
     * @return The manger for all dynamically added framebuffer attachments
     */
    @ApiStatus.Internal
    public DynamicBufferManager getDynamicBufferManger() {
        return this.dynamicBufferManager;
    }

    /**
     * @return The manager for all custom shader modifications
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated(forRemoval = true)
    public ShaderModificationManager getShaderModificationManager() {
        return this.shaderModificationManager;
    }

    /**
     * @return The manager for all shader injections
     */
    @ApiStatus.Internal
    public ShaderInjectionManager getShaderInjectionManager() {
        return this.shaderInjectionManager;
    }

    /**
     * @return The set of shader pre-definitions. Changes are automatically synced the next frame
     */
    public ShaderPreDefinitions getShaderDefinitions() {
        return this.shaderPreDefinitions;
    }

    /**
     * @return The manager for all veil shaders
     */
    public ShaderManager getShaderManager() {
        return this.shaderManager;
    }

    /**
     * @return The manager for all custom veil framebuffers
     */
    public FramebufferManager getFramebufferManager() {
        return this.framebufferManager;
    }

    /**
     * @return The manager for all {@link PostPipeline} instances
     */
    public PostProcessingManager getPostProcessingManager() {
        return this.postProcessingManager;
    }

    /**
     * @return The manager for all data-driven render types
     */
    @ApiStatus.Internal
    public DynamicRenderTypeManager getDynamicRenderTypeManager() {
        return this.dynamicRenderTypeManager;
    }

    /**
     * @return The manager for all quasar particles
     */
    public ParticleSystemManager getParticleManager() {
        return this.quasarParticleManager;
    }

    /**
     * @return The manager for rendering and managing effects
     * @since 2.5.0
     */
    public FlareEffectManager getEffectManager() {
        return this.flareEffectManager;
    }

    /**
     * @return The manager for all editors
     */
    public EditorManager getEditorManager() {
        return this.editorManager;
    }

    /**
     * @return The camera matrices instance
     */
    public CameraMatrices getCameraMatrices() {
        return this.cameraMatrices;
    }

    /**
     * @return The Veil light renderer instance
     */
    public LightRenderer getLightRenderer() {
        return this.lightRenderer;
    }

    /**
     * @return The gui info instance
     */
    public GuiInfo getGuiInfo() {
        return this.guiInfo;
    }

    // Internal Implementation

    @ApiStatus.Internal
    public void resize(int width, int height) {
        this.framebufferManager.resizeFramebuffers(width, height);
        this.dynamicBufferManager.resizeFramebuffers(width, height);

        // The old texture is deleted, so we have to remake the framebuffer
        VeilFirstPersonRenderer.free();
        VeilBloomRenderer.free();
    }

    @ApiStatus.Internal
    public void endFrame() {
        this.framebufferManager.clear();
        this.dynamicBufferManager.endFrame();
        this.postProcessingManager.endFrame();
    }

    @ApiStatus.Internal
    public void free() {
        this.dynamicBufferManager.free();
        this.shaderManager.close();
        this.framebufferManager.free();
        this.postProcessingManager.free();
        this.quasarParticleManager.clear();
        this.flareEffectManager.getShellManager().free();
        this.lightRenderer.free();
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        VeilBloomRenderer.tryEnable();
    }
}
