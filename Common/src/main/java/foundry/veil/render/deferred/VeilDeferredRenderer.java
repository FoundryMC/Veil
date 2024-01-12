package foundry.veil.render.deferred;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import foundry.veil.Veil;
import foundry.veil.render.framebuffer.AdvancedFbo;
import foundry.veil.render.framebuffer.FramebufferManager;
import foundry.veil.render.framebuffer.VeilFramebuffers;
import foundry.veil.render.post.PostPipeline;
import foundry.veil.render.post.PostProcessingManager;
import foundry.veil.render.shader.ShaderManager;
import foundry.veil.render.shader.definition.ShaderPreDefinitions;
import foundry.veil.render.wrapper.CullFrustum;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;

/**
 * <p>Handles mixing the regular deferred pipeline and the forward-rendered transparency pipeline.</p>
 * <p>The rendering pipeline goes in this order:</p>
 * <ul>
 *     <li>Opaque Shaders</li>
 *     <li>Opaque post-processing ({@link VeilDeferredRenderer#OPAQUE_POST})</li>
 *     <li>Light Shaders via {@link LightRenderer}</li>
 *     <li>Light post-processing ({@link VeilDeferredRenderer#LIGHT_POST})</li>
 *     <li>Transparency Shaders ({@link VeilDeferredRenderer#TRANSPARENT_POST})</li>
 *     <li>Skybox Shader(s) via {@link SkyRenderer}</li>
 *     <li>Final post-processing via {@link PostProcessingManager}</li>
 * </ul>
 *
 * @author Ocelot
 */
public class VeilDeferredRenderer implements PreparableReloadListener, NativeResource {

    public static final ResourceLocation PACK_ID = Veil.veilPath("deferred");
    public static final String DISABLE_VANILLA_ENTITY_LIGHT_KEY = "DISABLE_VANILLA_ENTITY_LIGHT";
    public static final String USE_BAKED_TRANSPARENT_LIGHTMAPS_KEY = "USE_BAKED_TRANSPARENT_LIGHTMAPS";

    public static final ResourceLocation OPAQUE_POST = Veil.veilPath("core/opaque");
    public static final ResourceLocation LIGHT_POST = Veil.veilPath("core/light");
    public static final ResourceLocation OPAQUE_MIX = Veil.veilPath("core/mix_opaque");
    public static final ResourceLocation TRANSPARENT_MIX = Veil.veilPath("core/mix_transparent");
    public static final ResourceLocation TRANSPARENT_POST = Veil.veilPath("core/transparent");
    public static final ResourceLocation SCREEN_POST = Veil.veilPath("core/screen");

    private static final Logger LOGGER = LogUtils.getLogger();

    private final ShaderManager deferredShaderManager;
    private final ShaderPreDefinitions shaderPreDefinitions;
    private final FramebufferManager framebufferManager;
    private final PostProcessingManager postProcessingManager;
    private final LightRenderer lightRenderer;

    private boolean enabled;
    private RendererState state;

    public VeilDeferredRenderer(ShaderManager deferredShaderManager, ShaderPreDefinitions shaderPreDefinitions, FramebufferManager framebufferManager, PostProcessingManager postProcessingManager) {
        this.deferredShaderManager = deferredShaderManager;
        this.shaderPreDefinitions = shaderPreDefinitions;
        this.framebufferManager = framebufferManager;
        this.postProcessingManager = postProcessingManager;
        this.lightRenderer = new LightRenderer();

        this.enabled = false;
        this.state = RendererState.INACTIVE;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        boolean active = resourceManager.listPacks().anyMatch(r -> r.packId().equals(PACK_ID.toString()));
        if (this.enabled != active) {
            this.enabled = active;
            if (active) {
                LOGGER.info("Deferred Renderer Enabled");
//                this.lightRenderer.addLight(new DirectionalLight());
                this.shaderPreDefinitions.define(USE_BAKED_TRANSPARENT_LIGHTMAPS_KEY);
            } else {
                LOGGER.info("Deferred Renderer Disabled");
                return preparationBarrier.wait(null).thenRunAsync(this::free, gameExecutor);
            }
        }

        if (this.enabled) {
            return this.deferredShaderManager.reload(preparationBarrier, resourceManager, prepareProfiler, applyProfiler, backgroundExecutor, gameExecutor);
        }

        return preparationBarrier.wait(null);
    }

    @Override
    public void free() {
        this.enabled = false;
        this.state = RendererState.INACTIVE;
        this.deferredShaderManager.close();
        this.lightRenderer.free();
    }

    @ApiStatus.Internal
    public void setup() {
        if (!this.isEnabled() || this.state == RendererState.DISABLED) {
            return;
        }

        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("veil_deferred");
        switch (this.state) {
            case OPAQUE -> {
                AdvancedFbo deferredFramebuffer = this.framebufferManager.getFramebuffer(VeilFramebuffers.DEFERRED);
                if (deferredFramebuffer == null) {
                    this.free();
                    return;
                }

                deferredFramebuffer.bind(true);
            }
            case TRANSLUCENT -> {
                AdvancedFbo transparent = this.framebufferManager.getFramebuffer(VeilFramebuffers.TRANSPARENT);
                if (transparent == null) {
                    this.free();
                    return;
                }

                transparent.bind(true);
            }
        }
        profiler.pop();

        // Temporary hack until we blit
//        deferredFramebuffer.bindDraw(false);
//        glDrawBuffers(GL_COLOR_ATTACHMENT0);
//        AdvancedFbo.getMainFramebuffer().resolveToAdvancedFbo(deferredFramebuffer);
//        deferredFramebuffer.bind(true);
//        glDrawBuffers(deferredFramebuffer.getDrawBuffers());
    }

    @ApiStatus.Internal
    public void clear() {
        if (!this.isEnabled() || this.state == RendererState.DISABLED) {
            return;
        }

        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("veil_deferred");
        switch (this.state) {
            case OPAQUE, TRANSLUCENT -> AdvancedFbo.unbind();
        }
        profiler.pop();
    }

    @ApiStatus.Internal
    public void beginOpaque() {
        if (!this.isEnabled() || this.state == RendererState.DISABLED) {
            return;
        }

        this.state = RendererState.OPAQUE;
        AdvancedFbo deferred = this.framebufferManager.getFramebuffer(VeilFramebuffers.DEFERRED);
        if (deferred == null) {
            this.free();
        }
    }

    @ApiStatus.Internal
    public void beginTranslucent() {
        if (!this.isEnabled() || this.state == RendererState.DISABLED) {
            return;
        }

        this.state = RendererState.TRANSLUCENT;
        AdvancedFbo transparent = this.framebufferManager.getFramebuffer(VeilFramebuffers.TRANSPARENT);
        if (transparent == null) {
            this.free();
            return;
        }

        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("veil_deferred");

        // Copy opaque depth to transparency, so it doesn't draw on top
        AdvancedFbo deferredFramebuffer = this.framebufferManager.getFramebuffer(VeilFramebuffers.DEFERRED);
        if (deferredFramebuffer != null) {
            deferredFramebuffer.resolveToAdvancedFbo(transparent, GL_DEPTH_BUFFER_BIT, GL_NEAREST);
        }

        profiler.pop();
    }

    private void run(CullFrustum frustum, AdvancedFbo deferred, AdvancedFbo light, ResourceLocation post, ResourceLocation mix) {
        PostPipeline postPipeline = this.postProcessingManager.getPipeline(post);
        if (postPipeline != null) {
            this.postProcessingManager.runPipeline(postPipeline);
        }

        light.bind(true);
        light.clear();
        this.lightRenderer.render(frustum, deferred);

        // Applies effects to the final light image
        PostPipeline lightPipeline = this.postProcessingManager.getPipeline(LIGHT_POST);
        if (lightPipeline != null) {
            this.postProcessingManager.runPipeline(lightPipeline);
        }

        // Applies light to the image
        PostPipeline mixOpaquePipeline = this.postProcessingManager.getPipeline(mix);
        if (mixOpaquePipeline != null) {
            this.postProcessingManager.runPipeline(mixOpaquePipeline);
        }
    }

    @ApiStatus.Internal
    public void blit(CullFrustum frustum) {
        if (!this.isEnabled() || this.state == RendererState.DISABLED) {
            return;
        }

        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("veil_deferred");

        this.end();

        AdvancedFbo deferred = this.framebufferManager.getFramebuffer(VeilFramebuffers.DEFERRED);
        AdvancedFbo transparent = this.framebufferManager.getFramebuffer(VeilFramebuffers.TRANSPARENT);
        AdvancedFbo light = this.framebufferManager.getFramebuffer(VeilFramebuffers.LIGHT);
        AdvancedFbo post = this.framebufferManager.getFramebuffer(VeilFramebuffers.POST);
        if (deferred == null || transparent == null || light == null || post == null) {
            this.free();
            return;
        }

        this.run(frustum, deferred, light, OPAQUE_POST, OPAQUE_MIX);
        this.run(frustum, transparent, light, TRANSPARENT_POST, TRANSPARENT_MIX);

        // Draws the final opaque image and transparent onto the background
        PostPipeline screenPipeline = this.postProcessingManager.getPipeline(SCREEN_POST);
        if (screenPipeline != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            this.postProcessingManager.runPipeline(screenPipeline);
            RenderSystem.disableBlend();
        }

        post.resolveToFramebuffer(Minecraft.getInstance().getMainRenderTarget());

        profiler.pop();
    }

    @ApiStatus.Internal
    public void end() {
        if (!this.isEnabled() || this.state == RendererState.DISABLED) {
            return;
        }

        this.state = RendererState.INACTIVE;
    }

    @ApiStatus.Internal
    public void addDebugInfo(Consumer<String> consumer) {
        boolean vanillaLights = this.lightRenderer.isVanillaLightEnabled();
        boolean vanillaEntityLights = this.shaderPreDefinitions.getDefinition(DISABLE_VANILLA_ENTITY_LIGHT_KEY) == null;
        boolean bakeTransparencyLightmaps =  this.shaderPreDefinitions.getDefinition(VeilDeferredRenderer.USE_BAKED_TRANSPARENT_LIGHTMAPS_KEY) != null;
        consumer.accept("Vanilla Light: " + (vanillaLights ? ChatFormatting.GREEN + "On" : ChatFormatting.RED + "Off"));
        consumer.accept("Vanilla Entity Light: " + (vanillaEntityLights ? ChatFormatting.GREEN + "On" : ChatFormatting.RED + "Off"));
        consumer.accept("Bake Transparency Lightmap: " + (bakeTransparencyLightmaps ? ChatFormatting.GREEN + "On" : ChatFormatting.RED + "Off"));
        this.lightRenderer.addDebugInfo(consumer);
    }

    /**
     * Allows the renderer to run normally.
     */
    public void enable() {
        this.state = RendererState.INACTIVE;
    }

    /**
     * Forces the renderer off.
     */
    public void disable() {
        this.state = RendererState.DISABLED;
    }

    /**
     * @return Whether the deferred renderer is initialized and ready to use
     */
    public boolean isEnabled() {
        return this.enabled && !Minecraft.useShaderTransparency(); // TODO allow fabulous
    }

    /**
     * @return Whether the deferred renderer is currently actively being used
     */
    public boolean isActive() {
        return this.isEnabled() && this.state.isActive();
    }

    public LightRenderer getLightRenderer() {
        return this.lightRenderer;
    }

    public RendererState getRendererState() {
        return this.state;
    }

    public ShaderManager getDeferredShaderManager() {
        return this.deferredShaderManager;
    }

    public enum RendererState {
        DISABLED, INACTIVE, OPAQUE, TRANSLUCENT;

        public boolean isActive() {
            return this == OPAQUE || this == TRANSLUCENT;
        }
    }
}
