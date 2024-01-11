package foundry.veil.render.deferred;

import com.mojang.logging.LogUtils;
import foundry.veil.Veil;
import foundry.veil.render.framebuffer.AdvancedFbo;
import foundry.veil.render.framebuffer.FramebufferManager;
import foundry.veil.render.framebuffer.VeilFramebuffers;
import foundry.veil.render.post.PostProcessingManager;
import foundry.veil.render.shader.ShaderManager;
import foundry.veil.render.shader.definition.ShaderPreDefinitions;
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
 *     <li>Opaque post-processing ({@link PostProcessingManager#OPAQUE_POST})</li>
 *     <li>Light Shaders via {@link LightRenderer}</li>
 *     <li>Light post-processing ({@link PostProcessingManager#LIGHT_POST})</li>
 *     <li>Transparency Shaders ({@link PostProcessingManager#TRANSPARENT_BLIT})</li>
 *     <li>Skybox Shader(s) via {@link SkyRenderer}</li>
 *     <li>Final post-processing via {@link PostProcessingManager}</li>
 * </ul>
 *
 * @author Ocelot
 */
public class VeilDeferredRenderer implements PreparableReloadListener, NativeResource {

    public static final ResourceLocation PACK_ID = Veil.veilPath("deferred");
    public static final String DISABLE_VANILLA_ENTITY_LIGHT_KEY = "DISABLE_VANILLA_ENTITY_LIGHT";
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ShaderManager deferredShaderManager;
    private final ShaderPreDefinitions shaderPreDefinitions;
    private final FramebufferManager framebufferManager;
    private final PostProcessingManager postProcessingManager;

    private boolean enabled;
    private RendererState state;

    public VeilDeferredRenderer(ShaderManager deferredShaderManager, ShaderPreDefinitions shaderPreDefinitions, FramebufferManager framebufferManager, PostProcessingManager postProcessingManager) {
        this.deferredShaderManager = deferredShaderManager;
        this.shaderPreDefinitions = shaderPreDefinitions;
        this.framebufferManager = framebufferManager;
        this.postProcessingManager = postProcessingManager;
        this.state = RendererState.INACTIVE;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        boolean active = resourceManager.listPacks().anyMatch(r -> r.packId().equals(PACK_ID.toString()));
        if (this.enabled != active) {
            this.enabled = active;
            if (active) {
                LOGGER.info("Deferred Renderer Enabled");
                // TODO setup
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
        this.state = RendererState.DISABLED;
        this.deferredShaderManager.close();
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

    @ApiStatus.Internal
    public void blit() {
        if (!this.isEnabled() || this.state == RendererState.DISABLED) {
            return;
        }

        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("veil_deferred");
        this.end();
        profiler.pop();

//        AdvancedFbo deferredFramebuffer = this.framebufferManager.getFramebuffer(VeilFramebuffers.DEFERRED);
//        if (deferredFramebuffer != null) {
//            deferredFramebuffer.resolveToFramebuffer(Minecraft.getInstance().getMainRenderTarget());
//        }
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
        if (this.state == RendererState.DISABLED) {
            consumer.accept("Disabled");
        }
        boolean vanillaEntityLights = this.shaderPreDefinitions.getDefinition(DISABLE_VANILLA_ENTITY_LIGHT_KEY) == null;
        consumer.accept("Vanilla Entity Lights: " + (vanillaEntityLights ? ChatFormatting.GREEN + "On" : ChatFormatting.RED + "Off"));
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
