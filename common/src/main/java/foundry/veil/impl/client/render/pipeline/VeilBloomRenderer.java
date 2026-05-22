package foundry.veil.impl.client.render.pipeline;

import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.FramebufferAttachmentDefinition;
import foundry.veil.api.client.render.framebuffer.FramebufferStack;
import foundry.veil.api.client.render.framebuffer.VeilFramebuffers;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.compat.IrisCompat;
import foundry.veil.api.compat.VeilVRCompat;
import foundry.veil.impl.client.VeilClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;

@ApiStatus.Internal
public final class VeilBloomRenderer {

    private static final ResourceLocation BLOOM_PIPELINE = Veil.veilPath("core/bloom");

    private static boolean enabled;
    private static boolean rendered;
    private static AdvancedFbo bloom;
    private static final boolean[] vrRendered = new boolean[2];
    private static final AdvancedFbo[] vrBloom = new AdvancedFbo[2];
    private static @Nullable AdvancedFbo sourceFramebuffer;

    public static void tryEnable() {
        boolean wasEnabled = enabled;
        enabled = getPipeline() != null;
        if (wasEnabled != enabled) {
            if (enabled) {
                Veil.LOGGER.info("Enabled bloom pipeline");
                rendered = false;
            } else {
                Veil.LOGGER.warn("Disabled bloom pipeline due to error");
                free();
            }
        }
    }

    public static void setupRenderState() {
        if (!enabled) {
            return;
        }

        if (isBloomDisabledForVr()) {
            setRendered(getActiveEye(), false);
            return;
        }

        if (IrisCompat.INSTANCE != null && IrisCompat.INSTANCE.areShadersLoaded()) {
            return;
        }

        AdvancedFbo mainRenderTarget = getSourceFramebuffer();
        int w = getBloomWidth(mainRenderTarget);
        int h = getBloomHeight(mainRenderTarget);
        int framebufferTexture = mainRenderTarget.getDepthTextureAttachment().getId();
        int eye = getActiveEye();
        AdvancedFbo activeBloom = getBloom(eye);
        if (activeBloom == null || activeBloom.getWidth() != w || activeBloom.getHeight() != h) {
            if (eye >= 0 && activeBloom != null) {
                VeilVRCompat.recordVrFramebufferResize(eye);
            }
            freeBloom(eye);
            activeBloom = AdvancedFbo.withSize(w, h)
                    .setFormat(FramebufferAttachmentDefinition.Format.RGBA16F)
                    .addColorTextureBuffer()
                    .setDepthTextureWrapper(framebufferTexture)
                    .setDebugLabel("Veil Bloom")
                    .build(true);
            setBloom(eye, activeBloom);
            if (eye >= 0) {
                VeilVRCompat.recordVrFramebufferCreation(eye);
            }
        } else if (activeBloom.isDepthMutableTextureAttachment()) {
            activeBloom.setDepthAttachmentTexture(framebufferTexture);
        }

        FramebufferStack.push(null);
        VeilRenderSystem.renderer().getFramebufferManager().setFramebuffer(VeilFramebuffers.BLOOM, activeBloom);
        activeBloom.bind(true);
        setRendered(eye, true);
    }

    public static void clearRenderState() {
        if (!enabled) {
            return;
        }

        if (isBloomDisabledForVr()) {
            return;
        }

        if (IrisCompat.INSTANCE != null && IrisCompat.INSTANCE.areShadersLoaded()) {
            return;
        }

        FramebufferStack.pop(null);
    }

    private static @Nullable PostPipeline getPipeline() {
        PostPipeline pipeline = VeilRenderSystem.renderer().getPostProcessingManager().getPipeline(BLOOM_PIPELINE);
        if (pipeline == null) {
            Veil.LOGGER.error("Failed to apply bloom pipeline");
        }
        return pipeline;
    }

    public static boolean hasRendered() {
        return getRendered(getActiveEye()) && enabled && !isBloomDisabledForVr();
    }

    public static void flush() {
        int eye = getActiveEye();
        if (!getRendered(eye) || !enabled || isBloomDisabledForVr()) {
            return;
        }

        setRendered(eye, false);
        PostPipeline pipeline = getPipeline();
        if (pipeline == null) {
            enabled = false;
            return;
        }

        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("bloom");

        FramebufferStack.push(null);
        VeilRenderSystem.renderer().getPostProcessingManager().runPipeline(pipeline);
        AdvancedFbo activeBloom = getBloom(eye);
        if (activeBloom != null) {
            activeBloom.clear(GL_COLOR_BUFFER_BIT);
        }
        FramebufferStack.pop(null);

        profiler.pop();
    }

    public static void free() {
        sourceFramebuffer = null;
        VeilRenderSystem.renderer().getFramebufferManager().removeFramebuffer(VeilFramebuffers.BLOOM);
        freeBloom(-1);
        for (int i = 0; i < vrBloom.length; i++) {
            freeBloom(i);
        }
    }

    public static void setSourceFramebuffer(@Nullable AdvancedFbo framebuffer) {
        sourceFramebuffer = framebuffer;
    }

    private static AdvancedFbo getSourceFramebuffer() {
        return sourceFramebuffer != null ? sourceFramebuffer : VeilVRCompat.getMainFramebufferOrDefault(AdvancedFbo.getMainFramebuffer());
    }

    private static int getActiveEye() {
        return VeilVRCompat.usesPerEyePostProcessing() ? VeilVRCompat.getActiveEyeIndex() : -1;
    }

    private static boolean isBloomDisabledForVr() {
        return VeilClientConfig.get().disableBloomInVR && VeilVRCompat.usesPerEyePostProcessing();
    }

    private static int getBloomWidth(AdvancedFbo mainRenderTarget) {
        float scale = VeilVRCompat.usesPerEyePostProcessing() ? VeilVRCompat.getVrBloomQuality() : 1.0F;
        return Math.max(1, Math.round(mainRenderTarget.getWidth() * scale));
    }

    private static int getBloomHeight(AdvancedFbo mainRenderTarget) {
        float scale = VeilVRCompat.usesPerEyePostProcessing() ? VeilVRCompat.getVrBloomQuality() : 1.0F;
        return Math.max(1, Math.round(mainRenderTarget.getHeight() * scale));
    }

    private static AdvancedFbo getBloom(int eye) {
        return eye >= 0 ? vrBloom[eye] : bloom;
    }

    private static void setBloom(int eye, AdvancedFbo framebuffer) {
        if (eye >= 0) {
            vrBloom[eye] = framebuffer;
        } else {
            bloom = framebuffer;
        }
    }

    private static boolean getRendered(int eye) {
        return eye >= 0 ? vrRendered[eye] : rendered;
    }

    private static void setRendered(int eye, boolean rendered) {
        if (eye >= 0) {
            vrRendered[eye] = rendered;
        } else {
            VeilBloomRenderer.rendered = rendered;
        }
    }

    private static void freeBloom(int eye) {
        AdvancedFbo framebuffer = getBloom(eye);
        if (framebuffer != null) {
            framebuffer.free();
            setBloom(eye, null);
        }
        setRendered(eye, false);
    }
}
