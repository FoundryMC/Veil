package foundry.veil.impl.client.render.dynamicbuffer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.dynamicbuffer.DynamicBufferType;
import foundry.veil.api.client.render.dynamicbuffer.DynamicBuffersChange;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.FramebufferManager;
import foundry.veil.api.compat.ImmersivePortalsCompat;
import foundry.veil.ext.RenderTargetExtension;
import foundry.veil.ext.ShaderInstanceExtension;
import foundry.veil.mixin.dynamicbuffer.accessor.DynamicBufferGameRendererAccessor;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;

import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL14C.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL30C.*;

@ApiStatus.Internal
public class DynamicBufferManager implements NativeResource {

    public static final ResourceLocation MAIN_WRAPPER = Veil.veilPath("dynamic_main");
    private static final DynamicBufferType[] BUFFERS = DynamicBufferType.values();
    private static final long MAX_SHADER_COMPILE_TIME_NS = 2 * 1_000_000; // millisecond -> nanosecond

    private int activeBuffers;
    private final Object2IntMap<ResourceLocation> activeBufferLayers;
    private boolean enabled;
    private final int[] clearBuffers;
    private final Map<ResourceLocation, AdvancedFbo> framebuffers;
    private final List<AdvancedFbo> dynamicFramebuffers;
    private final EnumMap<DynamicBufferType, DynamicBuffer> dynamicBuffers;
    private final Set<ShaderInstance> swapShaders;
    private int dynamicFboPointer;

    public DynamicBufferManager(int width, int height) {
        this.activeBuffers = 0;
        this.activeBufferLayers = new Object2IntArrayMap<>();
        this.enabled = false;
        this.clearBuffers = Arrays.stream(DynamicBufferType.values()).mapToInt(type -> GL_COLOR_ATTACHMENT1 + type.ordinal()).toArray();
        this.framebuffers = new HashMap<>();
        this.dynamicFramebuffers = new ArrayList<>();
        this.dynamicBuffers = new EnumMap<>(DynamicBufferType.class);
        this.swapShaders = new HashSet<>();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer textures = stack.mallocInt(DynamicBufferType.values().length);
            glGenTextures(textures);
            for (DynamicBufferType value : DynamicBufferType.values()) {
                DynamicBuffer buffer = new DynamicBuffer(value, textures.get(value.ordinal()));
                buffer.init(width, height);
                this.dynamicBuffers.put(value, buffer);
            }
        }
    }

    private void deleteFramebuffers() {
        FramebufferManager framebufferManager = VeilRenderSystem.renderer().getFramebufferManager();
        for (Map.Entry<ResourceLocation, AdvancedFbo> entry : this.framebuffers.entrySet()) {
            entry.getValue().free();
            framebufferManager.removeFramebuffer(entry.getKey());
        }
        this.framebuffers.clear();
        for (AdvancedFbo fbo : this.dynamicFramebuffers) {
            fbo.free();
        }
        this.dynamicFramebuffers.clear();
        this.dynamicFboPointer = 0;
    }

    public int getActiveBuffers(ResourceLocation name) {
        return this.activeBufferLayers.getOrDefault(name, 0);
    }

    public int getActiveBuffers() {
        return this.activeBuffers;
    }

    public int getBufferTexture(DynamicBufferType buffer) {
        if ((this.activeBuffers & buffer.getMask()) != 0) {
            int index = 1 + Integer.bitCount(this.activeBuffers & (buffer.getMask() - 1));
            int texture = ((RenderTargetExtension) Minecraft.getInstance().getMainRenderTarget()).veil$getTexture(index);
            if (texture != 0) {
                return texture;
            }
            return this.dynamicBuffers.get(buffer).textureId;
        }
        return MissingTextureAtlasSprite.getTexture().getId();
    }

    public boolean setActiveBuffers(ResourceLocation name, int activeBuffers) {
        if (Veil.IRIS) {
            return false;
        }

        int buffers = this.activeBufferLayers.getOrDefault(name, 0);
        if (buffers == activeBuffers) {
            return false;
        }

        if (activeBuffers == 0) {
            this.activeBufferLayers.removeInt(name);
        } else {
            this.activeBufferLayers.put(name, activeBuffers);
        }

        int flags = 0;
        for (int value : this.activeBufferLayers.values()) {
            flags |= value;
        }
        if (flags == this.activeBuffers) {
            return false;
        }

        int oldActiveBuffers = this.activeBuffers;
        this.activeBuffers = flags;
        this.deleteFramebuffers();

        VeilRenderer renderer = VeilRenderSystem.renderer();
        this.swapShaders.clear();

        DynamicBufferGameRendererAccessor accessor = (DynamicBufferGameRendererAccessor) Minecraft.getInstance().gameRenderer;
        for (ShaderInstance shader : accessor.getShaders().values()) {
            if (((ShaderInstanceExtension) shader).veil$swapBuffers(this.activeBuffers)) {
                this.swapShaders.add(shader);
            }
        }
        if (!this.swapShaders.isEmpty()) {
            renderer.getVanillaShaderCompiler().reload(this.swapShaders);
        }

        try {
            renderer.getShaderManager().setActiveBuffers(activeBuffers);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        VeilClient.clientPlatform().onVeilDynamicBuffersChanged(new DynamicBuffersChange(oldActiveBuffers, this.activeBuffers));
        return true;
    }

    public boolean isEnabled() {
        return this.activeBuffers != 0 && this.enabled;
    }

    public void setEnabled(boolean enabled) {
        if (!Veil.IRIS && (!ImmersivePortalsCompat.isLoaded() || !ImmersivePortalsCompat.INSTANCE.renderingThroughPortal())) {
            this.enabled = enabled;
        }
    }

    @Override
    public void free() {
        this.deleteFramebuffers();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer textures = stack.mallocInt(this.dynamicBuffers.size());
            for (DynamicBuffer buffer : this.dynamicBuffers.values()) {
                textures.put(buffer.textureId);
            }
            textures.rewind();
            glDeleteTextures(textures);
        }
        this.dynamicBuffers.clear();
    }

    /**
     * Sets up the rendering state for the specified target.
     *
     * @param name         The name of the framebuffer
     * @param renderTarget The render target to wrap or <code>null</code> to free
     * @param setViewport  Whether the viewport should also be set
     */
    public void setupRenderState(ResourceLocation name, @Nullable RenderTarget renderTarget, boolean setViewport) {
        if (!this.isEnabled()) {
            return;
        }

        if (renderTarget == null) {
            VeilRenderSystem.renderer().getFramebufferManager().removeFramebuffer(name);
            AdvancedFbo fbo = this.framebuffers.remove(name);
            if (fbo != null) {
                fbo.free();
            }
            // If the buffer doesn't exist, then try to bind the main framebuffer
            this.setupRenderState(MAIN_WRAPPER, Objects.requireNonNull(Minecraft.getInstance().getMainRenderTarget()), setViewport);
            return;
        }

        AdvancedFbo fbo = this.framebuffers.get(name);
        if (fbo == null) {
            AdvancedFbo.Builder builder = AdvancedFbo.withSize(renderTarget.width, renderTarget.height);
            builder.addColorTextureWrapper(renderTarget.getColorTextureId());
            for (Map.Entry<DynamicBufferType, DynamicBuffer> entry : this.dynamicBuffers.entrySet()) {
                DynamicBufferType type = entry.getKey();
                if ((this.activeBuffers & type.getMask()) != 0) {
                    builder.setName(type.getSourceName()).addColorTextureWrapper(entry.getValue().textureId);
                }
            }
            builder.setDepthTextureWrapper(renderTarget.getDepthTextureId());
            builder.setDebugLabel(name.toString());
            fbo = builder.build(true);
            this.framebuffers.put(name, fbo);
        }

        VeilRenderSystem.renderer().getFramebufferManager().setFramebuffer(name, fbo);
        fbo.bind(setViewport);
    }

    /**
     * Creates a dynamic fbo from the specified framebuffer. It will only write into the first color texture buffer and optionally the depth texture.
     *
     * @param framebuffer The framebuffer to add dynamic buffers to
     * @return The created buffer or <code>null</code> to use the input value
     */
    public AdvancedFbo getDynamicFbo(AdvancedFbo framebuffer) {
        if (!this.isEnabled()) {
            return framebuffer;
        }

        if (!framebuffer.isColorTextureAttachment(0)) {
            return framebuffer;
        }

        int colorTexture = framebuffer.getColorTextureAttachment(0).getId();

        if (this.dynamicFboPointer < this.dynamicFramebuffers.size()) {
            AdvancedFbo fbo = this.dynamicFramebuffers.get(this.dynamicFboPointer);
            if (fbo.getWidth() == framebuffer.getWidth() && fbo.getHeight() == framebuffer.getHeight()) {
                this.dynamicFboPointer++;
                fbo.setColorAttachmentTexture(0, colorTexture);
                if (framebuffer.isDepthTextureAttachment()) {
                    fbo.setDepthAttachmentTexture(framebuffer.getDepthTextureAttachment().getId());
                }
                fbo.clear(GL_COLOR_BUFFER_BIT, this.clearBuffers);
                return fbo;
            }
            this.dynamicFramebuffers.remove(this.dynamicFboPointer);
            fbo.free();
        }

        AdvancedFbo.Builder builder = AdvancedFbo.withSize(framebuffer.getWidth(), framebuffer.getHeight());
        builder.addColorTextureWrapper(colorTexture);
        for (Map.Entry<DynamicBufferType, DynamicBuffer> entry : this.dynamicBuffers.entrySet()) {
            DynamicBufferType type = entry.getKey();
            if ((this.activeBuffers & type.getMask()) != 0) {
//                if (createTextures) {
                builder.setName(type.getSourceName())
                        .setFormat(type.getTexelFormat(), type.getInternalFormat())
                        .addColorTextureBuffer();
//                } else {
//                    builder.setName(type.getSourceName())
//                            .addColorTextureWrapper(entry.getValue().textureId);
//                }
            }
        }
        if (framebuffer.isDepthTextureAttachment()) {
            builder.setDepthTextureWrapper(framebuffer.getDepthTextureAttachment().getId());
        } else {
            builder.setDepthTextureBuffer();
        }
        builder.setDebugLabel(framebuffer.getDebugLabel());
        AdvancedFbo fbo = builder.build(true);

        this.dynamicFramebuffers.add(this.dynamicFboPointer, fbo);
        this.dynamicFboPointer++;

        return fbo;
    }

    /**
     * @return The color buffers used for dynamic buffers for clearing
     */
    public int[] getClearBuffers() {
        return this.clearBuffers;
    }

    public void endFrame() {
        for (AdvancedFbo framebuffer : this.framebuffers.values()) {
            framebuffer.clear(0.0F, 0.0F, 0.0F, 0.0F, GL_COLOR_BUFFER_BIT, this.clearBuffers);
        }
        ListIterator<AdvancedFbo> iterator = this.dynamicFramebuffers.listIterator(this.dynamicFboPointer);
        while (iterator.hasNext()) {
            iterator.next().free();
            iterator.remove();
        }
        this.dynamicFboPointer = 0;

        if (this.swapShaders.isEmpty()) {
            return;
        }

        // When switching dynamic buffers all vanilla shader sources have to be uploaded before swapping, otherwise
        // the shader source will be processed multiple times
        // However, this causes a noticeable lag spike when switching dynamic buffers
        // This allocates a couple milliseconds to shader compilation on other frames

        long startTime = System.nanoTime();
        Iterator<ShaderInstance> shaderIterator = this.swapShaders.iterator();
        while (shaderIterator.hasNext() && System.nanoTime() - startTime < MAX_SHADER_COMPILE_TIME_NS) {
            ShaderInstanceExtension shader = (ShaderInstanceExtension) shaderIterator.next();
            if (!shader.veil$isRecompileReady(this.activeBuffers)) {
                continue;
            }

            boolean success = shader.veil$applyCompile();
            shaderIterator.remove();

            // If no real work was done, then try the next shader
            if (success) {
                break;
            }
        }

        if (!shaderIterator.hasNext()) {
            Veil.LOGGER.info("Finished uploading vanilla shaders");
            this.swapShaders.clear();
        }
    }

    public void markRecompiled(ShaderInstance shaderInstance) {
        this.swapShaders.add(shaderInstance);
    }

    public void resizeFramebuffers(int width, int height) {
        this.deleteFramebuffers();
        for (DynamicBuffer buffer : this.dynamicBuffers.values()) {
            buffer.resize(width, height);
        }
    }

    private record DynamicBuffer(DynamicBufferType type, int textureId) {

        public void init(int width, int height) {
            GlStateManager._bindTexture(this.textureId);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_LOD, 0);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAX_LOD, 0);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, 0.0F);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            GlStateManager._texImage2D(GL_TEXTURE_2D, 0, this.type.getInternalFormat(), width, height, 0, this.type.getTexelFormat(), GL_UNSIGNED_INT, null);
        }

        public void resize(int width, int height) {
            GlStateManager._bindTexture(this.textureId);
            GlStateManager._texImage2D(GL_TEXTURE_2D, 0, this.type.getInternalFormat(), width, height, 0, this.type.getTexelFormat(), GL_UNSIGNED_INT, null);
        }
    }
}
