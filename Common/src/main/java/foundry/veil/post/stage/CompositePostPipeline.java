package foundry.veil.post.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.framebuffer.AdvancedFbo;
import foundry.veil.framebuffer.FramebufferDefinition;
import foundry.veil.post.PostPipeline;
import foundry.veil.post.PostPipelineStageRegistry;
import foundry.veil.shader.texture.ShaderTextureSource;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A pipeline that runs all child pipelines in order.
 */
public class CompositePostPipeline implements PostPipeline {

    private static final Codec<Map<ResourceLocation, FramebufferDefinition>> FRAMEBUFFER_CODEC = Codec.unboundedMap(
            Codec.STRING.xmap(name -> new ResourceLocation("temp", name), ResourceLocation::getPath),
            FramebufferDefinition.CODEC);
    public static final Codec<CompositePostPipeline> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PostPipeline.CODEC.listOf().fieldOf("stages").forGetter(pipeline -> Arrays.asList(pipeline.getStages())),
            Codec.unboundedMap(Codec.STRING, ShaderTextureSource.CODEC)
                    .optionalFieldOf("textures", Collections.emptyMap())
                    .forGetter(CompositePostPipeline::getTextures),
            CompositePostPipeline.FRAMEBUFFER_CODEC
                    .optionalFieldOf("framebuffers", Collections.emptyMap())
                    .forGetter(CompositePostPipeline::getFramebuffers)
    ).apply(instance, (pipelines, textures, framebuffers) -> new CompositePostPipeline(pipelines.toArray(PostPipeline[]::new), textures, framebuffers)));

    private final PostPipeline[] stages;
    private final Map<String, ShaderTextureSource> textures;
    private final Map<ResourceLocation, FramebufferDefinition> framebufferDefinitions;
    private final Map<ResourceLocation, AdvancedFbo> framebuffers;

    private int screenWidth = -1;
    private int screenHeight = -1;

    /**
     * Creates a new composite post pipeline that runs all child pipelines in order.
     *
     * @param stages                 The pipelines to run in order
     * @param textures               The textures to bind globally
     * @param framebufferDefinitions The definitions of framebuffers to create in order to use in the stages
     */
    public CompositePostPipeline(PostPipeline[] stages, Map<String, ShaderTextureSource> textures, Map<ResourceLocation, FramebufferDefinition> framebufferDefinitions) {
        this.stages = stages;
        this.textures = textures;
        this.framebufferDefinitions = framebufferDefinitions;
        this.framebuffers = new HashMap<>();
    }

    @Override
    public void apply(Context context) {
        AdvancedFbo main = context.getDrawFramebuffer();
        if (this.screenWidth != main.getWidth() || this.screenHeight != main.getHeight()) {
            this.screenWidth = main.getWidth();
            this.screenHeight = main.getHeight();
            this.framebuffers.values().forEach(AdvancedFbo::free);
            this.framebuffers.clear();
            this.framebufferDefinitions.forEach((name, definition) -> this.framebuffers.put(name, definition.createBuilder(this.screenWidth, this.screenHeight).build(true)));
        }

        this.framebuffers.forEach(context::setFramebuffer);
        this.textures.forEach((name, texture) -> context.setSampler(name, texture.getId(context)));
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

    /**
     * @return The stages run in this pipeline
     */
    public PostPipeline[] getStages() {
        return this.stages;
    }

    /**
     * @return The globally bound textures for the child stages to access
     */
    public Map<String, ShaderTextureSource> getTextures() {
        return this.textures;
    }

    /**
     * @return The framebuffers created for the child stages to access
     */
    public Map<ResourceLocation, FramebufferDefinition> getFramebuffers() {
        return this.framebufferDefinitions;
    }
}
