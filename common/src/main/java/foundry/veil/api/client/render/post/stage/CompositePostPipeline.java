package foundry.veil.api.client.render.post.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.registry.PostPipelineStageRegistry;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.dynamicbuffer.DynamicBufferType;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.FramebufferDefinition;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.shader.texture.ShaderTextureSource;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.impl.client.render.shader.program.ShaderProgramImpl;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * A pipeline that runs all child pipelines in order.
 */
public final class CompositePostPipeline implements PostPipeline {

    private static final Codec<Map<ResourceLocation, FramebufferDefinition>> FRAMEBUFFER_CODEC = Codec.unboundedMap(
            Codec.STRING.xmap(name -> ResourceLocation.fromNamespaceAndPath("temp", name), ResourceLocation::getPath),
            FramebufferDefinition.CODEC);
    public static final Codec<CompositePostPipeline> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PostPipeline.CODEC.listOf().fieldOf("stages").forGetter(pipeline -> Arrays.asList(pipeline.getStages())),
            Codec.unboundedMap(Codec.STRING, ShaderTextureSource.CODEC)
                    .optionalFieldOf("textures", Collections.emptyMap())
                    .forGetter(CompositePostPipeline::getTextureSources),
            CompositePostPipeline.FRAMEBUFFER_CODEC
                    .optionalFieldOf("framebuffers", Collections.emptyMap())
                    .forGetter(CompositePostPipeline::getFramebuffers),
            VeilRenderLevelStageEvent.Stage.CODEC.optionalFieldOf("renderStage").forGetter(pipeline -> Optional.ofNullable(pipeline.getRenderStage())),
            DynamicBufferType.PACKED_LIST_CODEC.optionalFieldOf("dynamicBuffers", 0).forGetter(CompositePostPipeline::getDynamicBuffersMask),
            Codec.INT.optionalFieldOf("priority", 1000).forGetter(CompositePostPipeline::getPriority),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(CompositePostPipeline::isReplace)
    ).apply(instance, (pipelines, textures, framebuffers, renderStage, dynamicBuffers, priority, replace) -> new CompositePostPipeline(pipelines.toArray(PostPipeline[]::new), textures, framebuffers, renderStage.orElse(null), dynamicBuffers, priority, replace)));

    private final PostPipeline[] stages;
    private final Map<String, ShaderTextureSource> textureSources;
    private final Map<String, ShaderProgramImpl.ShaderTexture> samplers;
    private final Map<ResourceLocation, FramebufferDefinition> framebufferDefinitions;
    private final VeilRenderLevelStageEvent.Stage renderStage;
    private final Map<ResourceLocation, AdvancedFbo> framebuffers;
    private final Map<String, ShaderUniformAccess> uniforms;
    private final DynamicBufferType[] dynamicBuffers;
    private final int dynamicBuffersMask;
    private final int priority;
    private final boolean replace;

    private int screenWidth = -1;
    private int screenHeight = -1;

    private CompositePostPipeline(PostPipeline[] stages, Map<String, ShaderTextureSource> samplers, Map<ResourceLocation, FramebufferDefinition> framebufferDefinitions, @Nullable VeilRenderLevelStageEvent.Stage renderStage, int dynamicBuffers, int priority, boolean replace) {
        this.stages = stages;
        this.textureSources = Collections.unmodifiableMap(samplers);
        this.samplers = new Object2ObjectArrayMap<>(samplers.size());
        Minecraft.getInstance().execute(() -> {
            for (Map.Entry<String, ShaderTextureSource> entry : samplers.entrySet()) {
                this.samplers.put(entry.getKey(), ShaderProgramImpl.ShaderTexture.create(entry.getValue()));
            }
        });
        this.framebufferDefinitions = Collections.unmodifiableMap(framebufferDefinitions);
        this.renderStage = renderStage;
        this.framebuffers = new Object2ObjectArrayMap<>();
        this.uniforms = new Object2ObjectArrayMap<>();
        this.dynamicBuffers = DynamicBufferType.decode(dynamicBuffers);
        this.dynamicBuffersMask = dynamicBuffers;
        this.priority = priority;
        this.replace = replace;
    }

    /**
     * Creates a new composite post pipeline that runs all child pipelines in order.
     *
     * @param stages                 The pipelines to run in order
     * @param samplers               The textures to bind globally
     * @param framebufferDefinitions The definitions of framebuffers to create for use in the stages
     * @param renderStage            The stage in the renderer the pipeline should be applied at
     * @param dynamicBuffers         A bit field of all enabled dynamic buffers for this pipeline
     */
    public CompositePostPipeline(PostPipeline[] stages, Map<String, ShaderTextureSource> samplers, Map<ResourceLocation, FramebufferDefinition> framebufferDefinitions, @Nullable VeilRenderLevelStageEvent.Stage renderStage, int dynamicBuffers) {
        this(stages, samplers, framebufferDefinitions, renderStage, dynamicBuffers, 1000, false);
    }

    @Override
    public void apply(Context context) {
        AdvancedFbo main = context.getDrawFramebuffer();
        if (this.screenWidth != main.getWidth() || this.screenHeight != main.getHeight()) {
            this.screenWidth = main.getWidth();
            this.screenHeight = main.getHeight();
            this.framebuffers.values().forEach(AdvancedFbo::free);
            this.framebuffers.clear();

            MolangRuntime runtime = MolangRuntime.runtime()
                    .setQuery("screen_width", this.screenWidth)
                    .setQuery("screen_height", this.screenHeight)
                    .create();
            this.framebufferDefinitions.forEach((name, definition) -> this.framebuffers.put(name, definition.createBuilder(runtime)
                    .setDebugLabel("Temp " + name)
                    .build(true)));
        }

        this.framebuffers.forEach(context::setFramebuffer);
        for (DynamicBufferType buffer : this.dynamicBuffers) {
            context.setSampler(buffer.getSourceName(), VeilRenderSystem.renderer().getDynamicBufferManger().getBufferTexture(buffer), 0);
        }
        for (Map.Entry<String, ShaderProgramImpl.ShaderTexture> entry : this.samplers.entrySet()) {
            ShaderProgramImpl.ShaderTexture texture = entry.getValue();
            context.setSampler(entry.getKey(), texture.textureSource().getId(context), texture.samplerId());
        }
        for (PostPipeline pipeline : this.stages) {
            pipeline.apply(context);
        }
    }

    @Override
    public void free() {
        for (PostPipeline pipeline : this.stages) {
            pipeline.free();
        }
        this.framebuffers.values().forEach(AdvancedFbo::free);
        this.framebuffers.clear();
    }

    @Override
    public PostPipelineStageRegistry.PipelineType<? extends PostPipeline> getType() {
        throw new UnsupportedOperationException("Composite pipelines cannot be encoded");
    }

    @Override
    public boolean hasUniform(CharSequence name) {
        for (PostPipeline pipeline : this.stages) {
            if (pipeline.hasUniform(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable ShaderUniformAccess getUniform(CharSequence name) {
        ShaderUniformAccess uniform = this.getOrCreateUniform(name);
        return uniform == ShaderUniformAccess.EMPTY ? null : uniform;
    }

    @Override
    public ShaderUniformAccess getUniformSafe(CharSequence name) {
        return this.getOrCreateUniform(name);
    }

    @Override
    public ShaderUniformAccess getOrCreateUniform(CharSequence name) {
        return this.uniforms.computeIfAbsent(name.toString(), key -> ShaderUniformAccess.of(Arrays.stream(this.stages)
                .map(pipeline -> pipeline.getOrCreateUniform(key))
                .toArray(ShaderUniformAccess[]::new)));
    }

    @Override
    public boolean hasUniformBlock(CharSequence name) {
        for (PostPipeline pipeline : this.stages) {
            if (pipeline.hasUniformBlock(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasStorageBlock(CharSequence name) {
        for (PostPipeline pipeline : this.stages) {
            if (pipeline.hasStorageBlock(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setUniformBlock(CharSequence name, int binding) {
        for (PostPipeline pipeline : this.stages) {
            pipeline.setUniformBlock(name, binding);
        }
    }

    @Override
    public void setStorageBlock(CharSequence name, int binding) {
        for (PostPipeline pipeline : this.stages) {
            pipeline.setStorageBlock(name, binding);
        }
    }

    /**
     * @return The stages run in this pipeline
     */
    public PostPipeline[] getStages() {
        return this.stages;
    }

    /**
     * @return The globally bound textures for the child stages to access
     */
    public Map<String, ShaderTextureSource> getTextureSources() {
        return this.textureSources;
    }

    /**
     * @return The framebuffers created for the child stages to access
     */
    public Map<ResourceLocation, FramebufferDefinition> getFramebuffers() {
        return this.framebufferDefinitions;
    }

    /**
     * @return The dynamic buffers this pipeline uses
     */
    public DynamicBufferType[] getDynamicBuffers() {
        return this.dynamicBuffers;
    }

    /**
     * @return The render stage this pipeline should apply at or <code>null</code> to apply at the default time
     */
    public @Nullable VeilRenderLevelStageEvent.Stage getRenderStage() {
        return this.renderStage;
    }

    /**
     * @return The mask of dynamic buffers this pipeline uses
     */
    public int getDynamicBuffersMask() {
        return this.dynamicBuffersMask;
    }

    /**
     * @return The priority of this pipeline
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * @return Whether this stage will replace all stages with a higher priority
     */
    public boolean isReplace() {
        return this.replace;
    }
}
