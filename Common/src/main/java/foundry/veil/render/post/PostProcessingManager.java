package foundry.veil.render.post;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import foundry.veil.Veil;
import foundry.veil.render.framebuffer.AdvancedFbo;
import foundry.veil.render.framebuffer.FramebufferManager;
import foundry.veil.render.post.stage.CompositePostPipeline;
import foundry.veil.render.shader.ShaderManager;
import foundry.veil.render.shader.program.ShaderProgram;
import foundry.veil.resource.CodecReloadListener;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;
import org.slf4j.Logger;

import java.util.*;

import static org.lwjgl.opengl.GL11C.GL_ALWAYS;
import static org.lwjgl.opengl.GL11C.GL_LEQUAL;

/**
 * <p>Manages all post pipelines.</p>
 * <p>Post Pipelines are a single "effect" that can be applied.
 * For example, a vanilla Minecraft creeper effect can be a added using {@link #add(int, ResourceLocation)}</p>
 *
 * @author Ocelot
 */
public class PostProcessingManager extends CodecReloadListener<CompositePostPipeline> implements NativeResource {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Comparator<ProfileEntry> PIPELINE_SORTER = Comparator.comparingInt(ProfileEntry::priority).reversed();

    private final PostPipelineContext context;
    private final List<ProfileEntry> activePipelines;
    private final Map<ResourceLocation, PostPipeline> pipelines;

    /**
     * Creates a new instance of the post-processing manager.
     *
     * @param framebufferManager The manager for all custom framebuffers
     * @param textureManager     The texture manager instance
     * @param shaderManager      The shader manager instance
     */
    public PostProcessingManager(FramebufferManager framebufferManager, TextureManager textureManager, ShaderManager shaderManager) {
        super(CompositePostPipeline.CODEC, FileToIdConverter.json("pinwheel/post"));
        this.context = new PostPipelineContext(framebufferManager, textureManager, shaderManager);
        this.activePipelines = new LinkedList<>();
        this.pipelines = new HashMap<>();
        this.add(new ResourceLocation(Veil.MODID, "fog"));
        this.add(new ResourceLocation(Veil.MODID, "tonemap"));
        this.add(new ResourceLocation(Veil.MODID, "vignette"));
    }

    /**
     * Checks to see if the specified pipeline is active.
     *
     * @param pipeline The pipeline to check for
     * @return Whether that pipeline is active
     */
    public boolean isActive(ResourceLocation pipeline) {
        return this.activePipelines.stream().anyMatch(entry -> entry.pipeline.equals(pipeline));
    }

    /**
     * Adds the specified pipeline under the default priority of <code>1000</code>.
     * A higher priority indicates the pipeline should be run earlier than lower priority pipelines.
     *
     * @param pipeline The pipeline to add
     * @return Whether the pipeline was added or had a priority change
     */
    public boolean add(ResourceLocation pipeline) {
        return this.add(1000, pipeline);
    }

    /**
     * Adds the specified pipeline with the specified priority.
     * A higher priority indicates the pipeline should be run earlier than lower priority pipelines.
     *
     * @param priority The priority to set the pipeline to. The default priority is <code>1000</code>
     * @param pipeline The pipeline to add
     * @return Whether the pipeline was added or had a priority change
     */
    public boolean add(int priority, ResourceLocation pipeline) {
        if (this.activePipelines.stream().anyMatch(entry -> entry.priority == priority && entry.pipeline.equals(pipeline))) {
            return false;
        }
        this.remove(pipeline);
        this.activePipelines.add(new ProfileEntry(priority, pipeline));
        this.activePipelines.sort(PIPELINE_SORTER);
        return true;
    }

    /**
     * Removes the specified pipeline from the active profiles.
     *
     * @param pipeline The pipeline to remove
     * @return If the pipeline was previously active
     */
    public boolean remove(ResourceLocation pipeline) {
        return this.activePipelines.removeIf(entry -> entry.pipeline.equals(pipeline));
    }

    /**
     * Retrieves a post pipeline by name.
     *
     * @param pipeline The pipeline to get
     * @return The pipeline found or <code>null</code> if it doesn't exist
     */
    public @Nullable PostPipeline getPipeline(ResourceLocation pipeline) {
        return this.pipelines.get(pipeline);
    }

    private void setup() {
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL_ALWAYS);
    }

    private void clear() {
        ShaderProgram.unbind();
        AdvancedFbo.unbind();
        RenderSystem.depthFunc(GL_LEQUAL);
        RenderSystem.disableDepthTest();
    }

    private void clearPipeline() {
        RenderSystem.colorMask(true, true, true, true);
    }

    /**
     * Applies all pipelines in the order they are specified in the current pipeline list.
     */
    public void runPipeline() {
        if (this.activePipelines.isEmpty()) {
            return;
        }

        this.context.begin();
        this.setup();
        int activeTexture = GlStateManager._getActiveTexture();

        for (ProfileEntry entry : this.activePipelines) {
            PostPipeline pipeline = this.pipelines.get(entry.pipeline());
            if (pipeline != null) {
                try {
                    pipeline.apply(this.context);
                    this.clearPipeline();
                } catch (Exception e) {
                    LOGGER.error("Error running pipeline {}", entry.pipeline(), e);
                }
            }
        }

        RenderSystem.activeTexture(activeTexture);
        this.clear();
        this.context.end();
    }

    /**
     * Applies only the specified pipeline.
     *
     * @param pipeline The pipeline to run
     */
    public void runPipeline(PostPipeline pipeline) {
        this.context.begin();
        this.setup();
        int activeTexture = GlStateManager._getActiveTexture();

        try {
            pipeline.apply(this.context);
            this.clearPipeline();
        } catch (Exception e) {
            LOGGER.error("Error running pipeline {}", pipeline, e);
        }

        RenderSystem.activeTexture(activeTexture);
        this.clear();
        this.context.end();
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, CompositePostPipeline> data, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        this.pipelines.values().forEach(PostPipeline::free);
        this.pipelines.clear();
        this.pipelines.putAll(data);
        LOGGER.info("Loaded {} post pipelines", this.pipelines.size());
    }

    @Override
    public void free() {
        this.pipelines.values().forEach(PostPipeline::free);
        this.context.free();
    }

    /**
     * @return The default context for post-processing
     */
    public PostPipeline.Context getContext() {
        return this.context;
    }

    /**
     * @return A list of all active profiles and their priorities
     */
    public List<ProfileEntry> getActivePipelines() {
        return this.activePipelines;
    }

    /**
     * A single active profile.
     *
     * @param priority The priority the profile is inserted at
     * @param pipeline The id of the profile
     */
    public record ProfileEntry(int priority, ResourceLocation pipeline) {
    }
}
