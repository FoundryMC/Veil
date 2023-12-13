package foundry.veil.render.post;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import foundry.veil.VeilClient;
import foundry.veil.platform.services.VeilClientPlatform;
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
    private static final Comparator<ProfileEntry> PIPELINE_SORTER = Comparator.comparingInt(ProfileEntry::getPriority).reversed();

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
        this.activePipelines.add(new ProfileEntry(pipeline, priority));
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

        VeilClientPlatform platform = VeilClient.clientPlatform();
        this.context.begin();
        this.setup();
        int activeTexture = GlStateManager._getActiveTexture();

        this.activePipelines.sort(PIPELINE_SORTER);
        for (ProfileEntry entry : this.activePipelines) {
            ResourceLocation id = entry.getPipeline();
            PostPipeline pipeline = this.pipelines.get(id);
            if (pipeline != null) {
                platform.preVeilPostProcessing(id, pipeline);
                try {
                    pipeline.apply(this.context);
                    this.clearPipeline();
                } catch (Exception e) {
                    LOGGER.error("Error running pipeline {}", id, e);
                }
                platform.postVeilPostProcessing(id, pipeline);
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
     * @return All available pipelines
     */
    public @NotNull Set<ResourceLocation> getPipelines() {
        return this.pipelines.keySet();
    }

    /**
     * @return A list of all active profiles and their priorities
     */
    public List<ProfileEntry> getActivePipelines() {
        this.activePipelines.sort(PIPELINE_SORTER);
        return this.activePipelines;
    }

    /**
     * A single active profile.
     */
    public static class ProfileEntry {

        private final ResourceLocation pipeline;
        private int priority;

        public ProfileEntry(ResourceLocation pipeline, int priority) {
            this.pipeline = pipeline;
            this.priority = priority;
        }

        /**
         * @return The id of the pipeline shader
         */
        public ResourceLocation getPipeline() {
            return pipeline;
        }

        /**
         * @return The priority the profile is inserted at
         */
        public int getPriority() {
            return priority;
        }

        /**
         * Sets the priority this effect is applied at.
         *
         * @param priority The new priority
         */
        public void setPriority(int priority) {
            this.priority = priority;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ProfileEntry that = (ProfileEntry) o;
            return this.priority == that.priority && Objects.equals(pipeline, that.pipeline);
        }

        @Override
        public int hashCode() {
            int result = this.pipeline.hashCode();
            result = 31 * result + this.priority;
            return result;
        }
    }
}
