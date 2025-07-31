package foundry.veil.impl.client.render.pipeline;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.VeilFramebuffers;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.shader.program.TextureUniformAccess;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link PostPipeline.Context}.
 */
@ApiStatus.Internal
public class PostPipelineContext implements PostPipeline.Context {

    private final Object2LongMap<CharSequence> samplers;
    private final Map<ResourceLocation, AdvancedFbo> framebuffers;

    /**
     * Creates a new context to fit the specified window.
     */
    public PostPipelineContext() {
        this.samplers = new Object2LongArrayMap<>();
        this.framebuffers = new HashMap<>();
    }

    /**
     * Marks the start of a new post run.
     */
    public void begin() {
        VeilRenderSystem.renderer().getFramebufferManager().getFramebuffers().forEach(this::setFramebuffer);
    }

    /**
     * Ends the running pass and cleans up resources.
     */
    public void end() {
        this.samplers.clear();
        this.framebuffers.clear();
    }

    @Override
    public void setSampler(CharSequence name, int textureId, int samplerId) {
        this.samplers.put(name, (long) samplerId << 32 | textureId);
    }

    @Override
    public void setFramebuffer(ResourceLocation name, AdvancedFbo framebuffer) {
        this.framebuffers.put(name, framebuffer);
    }

    @Override
    public void applySamplers(TextureUniformAccess shader) {
        for (Object2LongMap.Entry<CharSequence> samplerEntry : this.samplers.object2LongEntrySet()) {
            long value = samplerEntry.getLongValue();
            int textureId = (int) (value & 0xFFFFFFFFL);
            int samplerId = (int) ((value >> 32) & 0xFFFFFFFFL);
            shader.setSampler(samplerEntry.getKey(), textureId, samplerId);
        }
    }

    @Override
    public void clearSamplers(TextureUniformAccess shader) {
        for (CharSequence name : this.samplers.keySet()) {
            shader.removeSampler(name);
        }
    }

    @Override
    public @Nullable AdvancedFbo getFramebuffer(ResourceLocation name) {
        return this.framebuffers.get(name);
    }

    @Override
    public AdvancedFbo getDrawFramebuffer() {
        return this.framebuffers.getOrDefault(VeilFramebuffers.POST, AdvancedFbo.getMainFramebuffer());
    }
}
