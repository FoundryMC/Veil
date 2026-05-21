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
import foundry.veil.api.compat.VeilVRCompat;
import foundry.veil.ext.RenderTargetExtension;
import foundry.veil.ext.ShaderInstanceExtension;
import foundry.veil.impl.client.VeilClientConfig;
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
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT1;

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
    private final Map<ResourceLocation, AdvancedFbo>[] vrFramebuffers;
    private final List<AdvancedFbo> dynamicFramebuffers;
    private final List<AdvancedFbo>[] vrDynamicFramebuffers;
    private final EnumMap<DynamicBufferType, DynamicBuffer> dynamicBuffers;
    private final EnumMap<DynamicBufferType, DynamicBuffer>[] vrDynamicBuffers;
    private int dynamicBufferWidth;
    private int dynamicBufferHeight;
    private final int[] vrDynamicBufferWidths;
    private final int[] vrDynamicBufferHeights;
    private final Set<ShaderInstance> swapShaders;
    private int dynamicFboPointer;
    private final int[] vrDynamicFboPointers;

    @SuppressWarnings("unchecked")
    public DynamicBufferManager(int width, int height) {
        this.activeBuffers = 0;
        this.activeBufferLayers = new Object2IntArrayMap<>();
        this.enabled = false;
        this.clearBuffers = Arrays.stream(DynamicBufferType.values()).mapToInt(type -> GL_COLOR_ATTACHMENT1 + type.ordinal()).toArray();
        this.framebuffers = new HashMap<>();
        this.vrFramebuffers = new Map[]{new HashMap<>(), new HashMap<>()};
        this.dynamicFramebuffers = new ArrayList<>();
        this.vrDynamicFramebuffers = new List[]{new ArrayList<>(), new ArrayList<>()};
        this.dynamicBuffers = this.createDynamicBuffers(width, height);
        this.vrDynamicBuffers = new EnumMap[]{this.createDynamicBuffers(width, height), this.createDynamicBuffers(width, height)};
        this.dynamicBufferWidth = width;
        this.dynamicBufferHeight = height;
        this.vrDynamicBufferWidths = new int[]{width, width};
        this.vrDynamicBufferHeights = new int[]{height, height};
        this.swapShaders = new HashSet<>();
        this.vrDynamicFboPointers = new int[2];
    }

    private EnumMap<DynamicBufferType, DynamicBuffer> createDynamicBuffers(int width, int height) {
        EnumMap<DynamicBufferType, DynamicBuffer> buffers = new EnumMap<>(DynamicBufferType.class);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer textures = stack.mallocInt(BUFFERS.length);
            glGenTextures(textures);
            for (DynamicBufferType value : BUFFERS) {
                DynamicBuffer buffer = new DynamicBuffer(value, textures.get(value.ordinal()));
                buffer.init(width, height);
                buffers.put(value, buffer);
            }
        }
        return buffers;
    }

    private int getActiveEyeIndex() {
        return VeilVRCompat.usesPerEyePostProcessing() ? VeilVRCompat.getActiveEyeIndex() : -1;
    }

    private Map<ResourceLocation, AdvancedFbo> getFramebuffers() {
        int eye = this.getActiveEyeIndex();
        return eye >= 0 ? this.vrFramebuffers[eye] : this.framebuffers;
    }

    private List<AdvancedFbo> getDynamicFramebuffers() {
        int eye = this.getActiveEyeIndex();
        return eye >= 0 ? this.vrDynamicFramebuffers[eye] : this.dynamicFramebuffers;
    }

    private EnumMap<DynamicBufferType, DynamicBuffer> getDynamicBuffers() {
        int eye = this.getActiveEyeIndex();
        return eye >= 0 ? this.vrDynamicBuffers[eye] : this.dynamicBuffers;
    }

    private int getDynamicFboPointer() {
        int eye = this.getActiveEyeIndex();
        return eye >= 0 ? this.vrDynamicFboPointers[eye] : this.dynamicFboPointer;
    }

    private void setDynamicFboPointer(int pointer) {
        int eye = this.getActiveEyeIndex();
        if (eye >= 0) {
            this.vrDynamicFboPointers[eye] = pointer;
        } else {
            this.dynamicFboPointer = pointer;
        }
    }

    private void deleteFramebuffers() {
        FramebufferManager framebufferManager = VeilRenderSystem.renderer().getFramebufferManager();
        this.deleteFramebufferMap(framebufferManager, this.framebuffers);
        for (Map<ResourceLocation, AdvancedFbo> framebuffers : this.vrFramebuffers) {
            this.deleteFramebufferMap(framebufferManager, framebuffers);
        }

        this.deleteDynamicFramebufferList(this.dynamicFramebuffers);
        for (List<AdvancedFbo> framebuffers : this.vrDynamicFramebuffers) {
            this.deleteDynamicFramebufferList(framebuffers);
        }
        this.dynamicFboPointer = 0;
        Arrays.fill(this.vrDynamicFboPointers, 0);
    }

    private void deleteFramebuffers(int eye) {
        FramebufferManager framebufferManager = VeilRenderSystem.renderer().getFramebufferManager();
        if (eye >= 0) {
            this.deleteFramebufferMap(framebufferManager, this.vrFramebuffers[eye]);
            this.deleteDynamicFramebufferList(this.vrDynamicFramebuffers[eye]);
            this.vrDynamicFboPointers[eye] = 0;
            return;
        }

        this.deleteFramebufferMap(framebufferManager, this.framebuffers);
        this.deleteDynamicFramebufferList(this.dynamicFramebuffers);
        this.dynamicFboPointer = 0;
    }

    private void deleteFramebufferMap(FramebufferManager framebufferManager, Map<ResourceLocation, AdvancedFbo> framebuffers) {
        for (Map.Entry<ResourceLocation, AdvancedFbo> entry : framebuffers.entrySet()) {
            entry.getValue().free();
            framebufferManager.removeFramebuffer(entry.getKey());
        }
        framebuffers.clear();
    }

    private void deleteDynamicFramebufferList(List<AdvancedFbo> framebuffers) {
        for (AdvancedFbo fbo : framebuffers) {
            fbo.free();
        }
        framebuffers.clear();
    }

    private void removeFramebuffer(ResourceLocation name) {
        FramebufferManager framebufferManager = VeilRenderSystem.renderer().getFramebufferManager();
        framebufferManager.removeFramebuffer(name);
        this.removeFramebuffer(name, this.framebuffers);
        for (Map<ResourceLocation, AdvancedFbo> framebuffers : this.vrFramebuffers) {
            this.removeFramebuffer(name, framebuffers);
        }
    }

    private void removeFramebuffer(ResourceLocation name, Map<ResourceLocation, AdvancedFbo> framebuffers) {
        AdvancedFbo fbo = framebuffers.remove(name);
        if (fbo != null) {
            fbo.free();
        }
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
            return this.getDynamicBuffers().get(buffer).textureId;
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
        if (!Veil.IRIS) {
            this.enabled = enabled;
        }
    }

    @Override
    public void free() {
        this.deleteFramebuffers();
        this.deleteDynamicBuffers(this.dynamicBuffers);
        for (EnumMap<DynamicBufferType, DynamicBuffer> buffers : this.vrDynamicBuffers) {
            this.deleteDynamicBuffers(buffers);
        }
    }

    private void deleteDynamicBuffers(EnumMap<DynamicBufferType, DynamicBuffer> buffers) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer textures = stack.mallocInt(buffers.size());
            for (DynamicBuffer buffer : buffers.values()) {
                textures.put(buffer.textureId);
            }
            textures.rewind();
            glDeleteTextures(textures);
        }
        buffers.clear();
    }

    private void ensureDynamicBuffersSize(int width, int height) {
        int eye = this.getActiveEyeIndex();
        if (eye >= 0) {
            if (this.vrDynamicBufferWidths[eye] == width && this.vrDynamicBufferHeights[eye] == height) {
                return;
            }

            this.resizeDynamicBuffers(this.vrDynamicBuffers[eye], width, height);
            this.vrDynamicBufferWidths[eye] = width;
            this.vrDynamicBufferHeights[eye] = height;
            this.deleteFramebuffers(eye);
            this.logDynamicBufferResize(eye, width, height);
            return;
        }

        if (this.dynamicBufferWidth == width && this.dynamicBufferHeight == height) {
            return;
        }

        this.resizeDynamicBuffers(this.dynamicBuffers, width, height);
        this.dynamicBufferWidth = width;
        this.dynamicBufferHeight = height;
        this.deleteFramebuffers(-1);
    }

    private void resizeDynamicBuffers(EnumMap<DynamicBufferType, DynamicBuffer> buffers, int width, int height) {
        for (DynamicBuffer buffer : buffers.values()) {
            buffer.resize(width, height);
        }
    }

    private void logDynamicBufferResize(int eye, int width, int height) {
        if (VeilClientConfig.get().debugVREyeBuffers) {
            Veil.LOGGER.info("Veil VR dynamic buffers resized: eye={} size={}x{}", eye == 0 ? "LEFT" : "RIGHT", width, height);
        }
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
            this.removeFramebuffer(name);
            // If the buffer doesn't exist, then try to bind the main framebuffer
            this.setupRenderState(MAIN_WRAPPER, Objects.requireNonNull(Minecraft.getInstance().getMainRenderTarget()), setViewport);
            return;
        }

        if (MAIN_WRAPPER.equals(name)) {
            renderTarget = Objects.requireNonNull(VeilVRCompat.getCurrentRenderTargetOrDefault(renderTarget));
        }
        this.ensureDynamicBuffersSize(renderTarget.width, renderTarget.height);

        Map<ResourceLocation, AdvancedFbo> framebuffers = this.getFramebuffers();
        EnumMap<DynamicBufferType, DynamicBuffer> dynamicBuffers = this.getDynamicBuffers();
        AdvancedFbo fbo = framebuffers.get(name);
        if (fbo == null) {
            AdvancedFbo.Builder builder = AdvancedFbo.withSize(renderTarget.width, renderTarget.height);
            builder.addColorTextureWrapper(renderTarget.getColorTextureId());
            for (Map.Entry<DynamicBufferType, DynamicBuffer> entry : dynamicBuffers.entrySet()) {
                DynamicBufferType type = entry.getKey();
                if ((this.activeBuffers & type.getMask()) != 0) {
                    builder.setName(type.getSourceName()).addColorTextureWrapper(entry.getValue().textureId);
                }
            }
            builder.setDepthTextureWrapper(renderTarget.getDepthTextureId());
            builder.setDebugLabel(name.toString());
            fbo = builder.build(true);
            framebuffers.put(name, fbo);
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
        List<AdvancedFbo> dynamicFramebuffers = this.getDynamicFramebuffers();
        EnumMap<DynamicBufferType, DynamicBuffer> dynamicBuffers = this.getDynamicBuffers();
        int dynamicFboPointer = this.getDynamicFboPointer();

        if (dynamicFboPointer < dynamicFramebuffers.size()) {
            AdvancedFbo fbo = dynamicFramebuffers.get(dynamicFboPointer);
            if (fbo.getWidth() == framebuffer.getWidth() && fbo.getHeight() == framebuffer.getHeight()) {
                this.setDynamicFboPointer(dynamicFboPointer + 1);
                fbo.setColorAttachmentTexture(0, colorTexture);
                if (framebuffer.isDepthTextureAttachment()) {
                    fbo.setDepthAttachmentTexture(framebuffer.getDepthTextureAttachment().getId());
                }
                fbo.clear(GL_COLOR_BUFFER_BIT, this.clearBuffers);
                return fbo;
            }
            dynamicFramebuffers.remove(dynamicFboPointer);
            fbo.free();
        }

        AdvancedFbo.Builder builder = AdvancedFbo.withSize(framebuffer.getWidth(), framebuffer.getHeight());
        builder.addColorTextureWrapper(colorTexture);
        for (Map.Entry<DynamicBufferType, DynamicBuffer> entry : dynamicBuffers.entrySet()) {
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

        dynamicFramebuffers.add(dynamicFboPointer, fbo);
        this.setDynamicFboPointer(dynamicFboPointer + 1);

        return fbo;
    }

    /**
     * @return The color buffers used for dynamic buffers for clearing
     */
    public int[] getClearBuffers() {
        return this.clearBuffers;
    }

    public void endFrame() {
        this.endFrame(this.framebuffers, this.dynamicFramebuffers, this.dynamicFboPointer);
        this.dynamicFboPointer = 0;
        for (int i = 0; i < this.vrFramebuffers.length; i++) {
            this.endFrame(this.vrFramebuffers[i], this.vrDynamicFramebuffers[i], this.vrDynamicFboPointers[i]);
            this.vrDynamicFboPointers[i] = 0;
        }

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
        }
    }

    private void endFrame(Map<ResourceLocation, AdvancedFbo> framebuffers, List<AdvancedFbo> dynamicFramebuffers, int dynamicFboPointer) {
        for (AdvancedFbo framebuffer : framebuffers.values()) {
            framebuffer.clear(0.0F, 0.0F, 0.0F, 0.0F, GL_COLOR_BUFFER_BIT, this.clearBuffers);
        }
        ListIterator<AdvancedFbo> iterator = dynamicFramebuffers.listIterator(dynamicFboPointer);
        while (iterator.hasNext()) {
            iterator.next().free();
            iterator.remove();
        }
    }

    public void markRecompiled(ShaderInstance shaderInstance) {
        this.swapShaders.add(shaderInstance);
    }

    public void resizeFramebuffers(int width, int height) {
        this.deleteFramebuffers();
        this.resizeDynamicBuffers(this.dynamicBuffers, width, height);
        this.dynamicBufferWidth = width;
        this.dynamicBufferHeight = height;
        Arrays.fill(this.vrDynamicBufferWidths, -1);
        Arrays.fill(this.vrDynamicBufferHeights, -1);
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
