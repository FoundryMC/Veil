package foundry.veil.impl.client.render.pipeline;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.VeilFramebuffers;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.shader.program.TextureUniformAccess;
import foundry.veil.api.compat.VeilVRCompat;
import foundry.veil.impl.client.render.shader.program.ShaderTextureCache;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
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

    private final Object2LongMap<CharSequence> textures;
    private final Object2IntMap<CharSequence> samplers;
    private final Map<ResourceLocation, AdvancedFbo> framebuffers;

    /**
     * Creates a new context to fit the specified window.
     */
    public PostPipelineContext() {
        this.textures = new Object2LongArrayMap<>();
        this.samplers = new Object2IntArrayMap<>();
        this.framebuffers = new HashMap<>();
    }

    /**
     * Marks the start of a new post run.
     */
    public void begin() {
        VeilRenderSystem.renderer().getFramebufferManager().getFramebuffers().forEach(this::setFramebuffer);
        VeilVRCompat.setupPostContext(this);
    }

    /**
     * Ends the running pass and cleans up resources.
     */
    public void end() {
        this.textures.clear();
        this.samplers.clear();
        this.framebuffers.clear();
    }

    @Override
    public void setTexture(CharSequence name, int target, int textureId, int samplerId) {
        this.textures.put(name, ShaderTextureCache.packTexture(target, textureId));
        this.samplers.put(name, samplerId);
    }

    @Override
    public void setFramebuffer(ResourceLocation name, AdvancedFbo framebuffer) {
        this.framebuffers.put(name, framebuffer);
    }

    @Override
    public void applySamplers(TextureUniformAccess shader) {
        for (Object2LongMap.Entry<CharSequence> samplerEntry : this.textures.object2LongEntrySet()) {
            CharSequence key = samplerEntry.getKey();
            long packed = samplerEntry.getLongValue();
            int target = ShaderTextureCache.getTarget(packed);
            int textureId = ShaderTextureCache.getTextureId(packed);
            shader.setTexture(key, target, textureId, this.samplers.getOrDefault(key, 0));
        }
    }

    @Override
    public void clearSamplers(TextureUniformAccess shader) {
        for (CharSequence name : this.textures.keySet()) {
            shader.removeTexture(name);
        }
    }

    @Override
    public @Nullable AdvancedFbo getFramebuffer(ResourceLocation name) {
        AdvancedFbo framebuffer = this.framebuffers.get(name);
        AdvancedFbo vrFramebuffer = VeilVRCompat.getFramebufferOrDefault(name, framebuffer);
        if (vrFramebuffer != framebuffer && vrFramebuffer != null) {
            this.framebuffers.put(name, vrFramebuffer);
        }
        return vrFramebuffer;
    }

    @Override
    public AdvancedFbo getDrawFramebuffer() {
        return this.framebuffers.getOrDefault(VeilFramebuffers.POST, AdvancedFbo.getMainFramebuffer());
    }
}
