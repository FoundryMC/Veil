package foundry.veil.api.compat;

import com.mojang.blaze3d.pipeline.RenderTarget;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.FramebufferAttachmentDefinition;
import foundry.veil.api.client.render.framebuffer.FramebufferDefinition;
import foundry.veil.api.client.render.framebuffer.VeilFramebuffers;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.impl.client.VeilClientConfig;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.opengl.GL11C.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11C.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL11C.glIsEnabled;

/**
 * Optional Vivecraft bridge. All Vivecraft access is reflective so Veil keeps loading normally without Vivecraft.
 */
@SuppressWarnings("unchecked")
public final class VeilVRCompat {

    private static final int LEFT_EYE = 0;
    private static final int RIGHT_EYE = 1;

    private static final EyeFramebuffer[] POST_FRAMEBUFFERS = new EyeFramebuffer[]{new EyeFramebuffer("Left"), new EyeFramebuffer("Right")};
    private static final Map<ResourceLocation, IsolatedFramebuffer>[] ISOLATED_FRAMEBUFFERS = new Map[]{new HashMap<>(), new HashMap<>()};
    private static final Set<ResourceLocation> WARNED_SHARED_BUFFERS = new HashSet<>();
    private static final Set<ResourceLocation> WARNED_ALPHA_CLEARS = new HashSet<>();
    private static final Deque<VrGlState> VR_GL_STATES = new ArrayDeque<>();
    private static AdvancedFbo vrMainFramebuffer;

    private static boolean initialized;
    private static boolean vivecraftDetected;
    private static boolean vivecraftApiAvailable;
    private static boolean loggedVrStatus;
    private static boolean lastStatusDetected;
    private static boolean lastStatusInitialized;
    private static boolean lastStatusActive;
    private static String lastStatusDevice = "";
    private static boolean loggedSodiumFallback;
    private static boolean cachedVrState;
    private static boolean cachedVrInitialized;
    private static boolean cachedVrActive;

    private static Object vrClientApi;
    private static Method isVrInitializedMethod;
    private static Method isVrActiveMethod;
    private static Object vrRenderingApi;
    private static Method getCurrentRenderPassMethod;
    private static Method isVanillaRenderPassMethod;
    private static Field vrInitializedField;
    private static Field vrRunningField;
    private static Method clientDataHolderGetInstanceMethod;
    private static Field currentPassField;
    private static Field vrField;
    private static Field vrRendererField;
    private static Field framebufferEye0Field;
    private static Field framebufferEye1Field;
    private static Method getVrProviderNameMethod;
    private static Method getVrRuntimeNameMethod;
    private static Field detectedHardwareField;
    private static Method getWorldRenderPassMethod;
    private static Field worldRenderPassTargetField;
    private static Field currentWorldRenderPassField;

    private VeilVRCompat() {
    }

    @ApiStatus.Internal
    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        vivecraftDetected = Veil.platform().isModLoaded("vivecraft");
        Veil.LOGGER.info("Vivecraft detected: {}", vivecraftDetected);
        if (!vivecraftDetected) {
            return;
        }

        try {
            Class<?> clientApiClass = Class.forName("org.vivecraft.api.client.VRClientAPI");
            vrClientApi = clientApiClass.getMethod("instance").invoke(null);
            isVrInitializedMethod = clientApiClass.getMethod("isVRInitialized");
            isVrActiveMethod = clientApiClass.getMethod("isVRActive");

            Class<?> renderingApiClass = Class.forName("org.vivecraft.api.client.VRRenderingAPI");
            vrRenderingApi = renderingApiClass.getMethod("instance").invoke(null);
            getCurrentRenderPassMethod = renderingApiClass.getMethod("getCurrentRenderPass");
            isVanillaRenderPassMethod = renderingApiClass.getMethod("isVanillaRenderPass");

            vivecraftApiAvailable = true;
        } catch (Throwable e) {
            Veil.LOGGER.warn("Vivecraft was detected, but its public client rendering API could not be linked. Falling back to internal reflective state.", e);
        }

        try {
            Class<?> vrStateClass = Class.forName("org.vivecraft.client_vr.VRState");
            vrInitializedField = vrStateClass.getField("VR_INITIALIZED");
            vrRunningField = vrStateClass.getField("VR_RUNNING");

            Class<?> holderClass = Class.forName("org.vivecraft.client_vr.ClientDataHolderVR");
            clientDataHolderGetInstanceMethod = holderClass.getMethod("getInstance");
            currentPassField = holderClass.getField("currentPass");
            vrField = holderClass.getField("vr");
            vrRendererField = holderClass.getField("vrRenderer");

            Class<?> vrClass = Class.forName("org.vivecraft.client_vr.provider.MCVR");
            getVrProviderNameMethod = vrClass.getMethod("getName");
            getVrRuntimeNameMethod = vrClass.getMethod("getRuntimeName");
            detectedHardwareField = vrClass.getDeclaredField("detectedHardware");
            detectedHardwareField.setAccessible(true);

            Class<?> vrRendererClass = Class.forName("org.vivecraft.client_vr.provider.VRRenderer");
            framebufferEye0Field = vrRendererClass.getField("framebufferEye0");
            framebufferEye1Field = vrRendererClass.getField("framebufferEye1");
        } catch (Throwable e) {
            if (!vivecraftApiAvailable) {
                Veil.LOGGER.warn("Vivecraft internal VR state was not available to Veil.", e);
            }
        }

        try {
            Class<?> renderPassClass = Class.forName("org.vivecraft.api.client.data.RenderPass");
            Class<?> worldRenderPassClass = Class.forName("org.vivecraft.client_xr.render_pass.WorldRenderPass");
            getWorldRenderPassMethod = worldRenderPassClass.getMethod("getByRenderPass", renderPassClass);
            worldRenderPassTargetField = worldRenderPassClass.getField("target");

            Class<?> renderPassManagerClass = Class.forName("org.vivecraft.client_xr.render_pass.RenderPassManager");
            currentWorldRenderPassField = renderPassManagerClass.getField("WRP");
        } catch (Throwable ignored) {
            // Older Vivecraft builds can still be handled through the VRRenderer eye targets above.
        }
    }

    /**
     * @return Whether Vivecraft is installed.
     */
    public static boolean isVivecraftDetected() {
        init();
        return vivecraftDetected;
    }

    /**
     * @return Whether Vivecraft reports VR as active.
     */
    public static boolean isVrActive() {
        refreshVrState();
        return cachedVrActive;
    }

    /**
     * @return Whether Vivecraft initialized its VR runtime successfully.
     */
    public static boolean isVrInitialized() {
        refreshVrState();
        return cachedVrInitialized;
    }

    /**
     * Clears the per-frame Vivecraft state cache. VR active/initialized status cannot change during a single render
     * frame, and caching it avoids repeated reflective Vivecraft calls from every post stage and light pass.
     */
    @ApiStatus.Internal
    public static void beginFrame() {
        cachedVrState = false;
    }

    private static void refreshVrState() {
        if (cachedVrState) {
            return;
        }

        init();
        if (!VeilClientConfig.get().enableVrCompatibility || !vivecraftDetected) {
            cachedVrInitialized = false;
            cachedVrActive = false;
            cachedVrState = true;
            logVrStatus();
            return;
        }

        Boolean initialized = invokeBoolean(vrClientApi, isVrInitializedMethod);
        if (initialized == null && vrInitializedField != null) {
            try {
                initialized = vrInitializedField.getBoolean(null);
            } catch (Throwable ignored) {
                initialized = false;
            }
        }

        cachedVrInitialized = Boolean.TRUE.equals(initialized);

        Boolean active = invokeBoolean(vrClientApi, isVrActiveMethod);
        if (active == null && vrRunningField != null) {
            try {
                active = vrRunningField.getBoolean(null);
            } catch (Throwable ignored) {
                active = false;
            }
        }

        cachedVrActive = Boolean.TRUE.equals(active);
        cachedVrState = true;
        logVrStatus();
    }

    /**
     * @return Whether the current render pass is one of Vivecraft's stereo eye passes.
     */
    public static boolean isRenderingVrEye() {
        return getActiveEyeIndex() >= 0;
    }

    /**
     * @return The current VR eye index, or {@code -1} when not rendering the left or right eye.
     */
    public static int getActiveEyeIndex() {
        if (!isVrActive()) {
            return -1;
        }
        return switch (getCurrentPassName()) {
            case "LEFT" -> LEFT_EYE;
            case "RIGHT" -> RIGHT_EYE;
            default -> -1;
        };
    }

    /**
     * @return The current Vivecraft render pass name if available.
     */
    public static String getCurrentPassName() {
        Object pass = getCurrentPass();
        return pass instanceof Enum<?> enumPass ? enumPass.name() : "UNKNOWN";
    }

    private static @Nullable Object getCurrentPass() {
        init();
        Object pass = invokeObject(vrRenderingApi, getCurrentRenderPassMethod);
        if (pass == null && clientDataHolderGetInstanceMethod != null && currentPassField != null) {
            Object holder = invokeObject(null, clientDataHolderGetInstanceMethod);
            if (holder != null) {
                try {
                    pass = currentPassField.get(holder);
                } catch (Throwable ignored) {
                    pass = null;
                }
            }
        }
        return pass;
    }

    /**
     * @return Whether Vivecraft currently considers this the vanilla pass.
     */
    public static boolean isVanillaRenderPass() {
        Boolean vanilla = invokeBoolean(vrRenderingApi, isVanillaRenderPassMethod);
        return vanilla == null || vanilla;
    }

    /**
     * Adds per-eye framebuffer overrides to a post-processing context.
     */
    @ApiStatus.Internal
    public static void setupPostContext(PostPipeline.Context context) {
        if (!usesPerEyePostProcessing()) {
            return;
        }

        AdvancedFbo post = getPostFramebuffer();
        if (post != null) {
            context.setFramebuffer(VeilFramebuffers.POST, post);
        }
    }

    /**
     * @return The current per-eye post framebuffer, or {@code null} when the VR path is inactive.
     */
    public static @Nullable AdvancedFbo getPostFramebuffer() {
        if (!usesPerEyePostProcessing()) {
            return null;
        }

        int eye = getActiveEyeIndex();
        RenderTarget target = getCurrentRenderTargetOrDefault(Minecraft.getInstance().getMainRenderTarget());
        if (eye < 0 || target == null) {
            return null;
        }
        return POST_FRAMEBUFFERS[eye].get(target);
    }

    private static @Nullable AdvancedFbo getIsolatedFramebuffer(int eye, ResourceLocation name, FramebufferDefinition definition, int width, int height) {
        Map<ResourceLocation, IsolatedFramebuffer> framebuffers = ISOLATED_FRAMEBUFFERS[eye];
        IsolatedFramebuffer isolated = framebuffers.get(name);
        if (isolated == null || !isolated.matches(definition, width, height)) {
            if (isolated != null) {
                isolated.free();
                recordVrFramebufferResize(eye);
            }

            try {
                MolangRuntime runtime = MolangRuntime.runtime()
                        .setQuery("screen_width", width)
                        .setQuery("screen_height", height)
                        .create();
                AdvancedFbo framebuffer = definition.createBuilder(runtime)
                        .setDebugLabel("Veil VR " + getEyeName(eye) + " " + name)
                        .build(true);
                framebuffer.clear();
                isolated = new IsolatedFramebuffer(definition, framebuffer, width, height);
                framebuffers.put(name, isolated);
                recordVrFramebufferCreation(eye);
            } catch (Exception e) {
                Veil.LOGGER.warn("Failed to create per-eye VR framebuffer {} for {} eye", name, getEyeName(eye), e);
                framebuffers.remove(name);
                return null;
            }
        }
        isolated.markUsed();
        return isolated.framebuffer;
    }

    /**
     * Returns a VR post framebuffer when active, otherwise the provided fallback.
     */
    public static @Nullable AdvancedFbo getPostFramebufferOrDefault(@Nullable AdvancedFbo fallback) {
        AdvancedFbo post = getPostFramebuffer();
        return post != null ? post : fallback;
    }

    /**
     * Returns a VR-isolated framebuffer for data-driven Veil framebuffers when active.
     */
    public static @Nullable AdvancedFbo getFramebufferOrDefault(ResourceLocation name, @Nullable AdvancedFbo fallback) {
        if (VeilFramebuffers.POST.equals(name)) {
            return getPostFramebufferOrDefault(fallback);
        }

        if (!usesPerEyePostProcessing() || isKnownPerEyeBuffer(name) || "temp".equals(name.getNamespace())) {
            return fallback;
        }

        int eye = getActiveEyeIndex();
        RenderTarget target = getCurrentRenderTargetOrDefault(Minecraft.getInstance().getMainRenderTarget());
        if (eye < 0 || target == null) {
            return fallback;
        }

        FramebufferDefinition definition = VeilRenderSystem.renderer().getFramebufferManager().getFramebufferDefinition(name);
        if (definition == null) {
            return fallback;
        }

        AdvancedFbo framebuffer = getIsolatedFramebuffer(eye, name, definition, getIsolatedWidth(name, target), getIsolatedHeight(name, target));
        return framebuffer != null ? framebuffer : fallback;
    }

    /**
     * Returns Vivecraft's active eye target when rendering a VR eye.
     */
    public static @Nullable RenderTarget getCurrentRenderTarget() {
        if (!usesPerEyePostProcessing()) {
            return null;
        }

        RenderTarget target = getWorldRenderPassTarget(getCurrentPass());
        if (target != null) {
            return target;
        }

        if (!VeilClientConfig.get().useSodiumCompatibleVrFallback) {
            return null;
        }

        // Render optimizers can replace or wrap parts of the vanilla target flow. Falling back to
        // Vivecraft's own eye FBO avoids assuming the Minecraft main target owns the current stereo image.
        if (!loggedSodiumFallback) {
            loggedSodiumFallback = true;
            Veil.LOGGER.info("Using Vivecraft eye framebuffer fallback for VR rendering.");
        }

        return getVrRendererEyeTarget(getActiveEyeIndex());
    }

    /**
     * Returns Vivecraft's active eye target when available, otherwise {@code fallback}.
     */
    public static @Nullable RenderTarget getCurrentRenderTargetOrDefault(@Nullable RenderTarget fallback) {
        RenderTarget target = getCurrentRenderTarget();
        return target != null ? target : fallback;
    }

    /**
     * Returns an AdvancedFbo wrapper over Vivecraft's active eye target when available.
     */
    public static AdvancedFbo getMainFramebufferOrDefault(AdvancedFbo fallback) {
        if (!usesPerEyePostProcessing()) {
            return fallback;
        }

        RenderTarget target = getCurrentRenderTarget();
        if (target == null) {
            return fallback;
        }

        if (vrMainFramebuffer == null) {
            vrMainFramebuffer = VeilRenderBridge.wrap(() -> getCurrentRenderTargetOrDefault(Minecraft.getInstance().getMainRenderTarget()));
        }
        return vrMainFramebuffer;
    }

    private static @Nullable RenderTarget getWorldRenderPassTarget(@Nullable Object pass) {
        if (worldRenderPassTargetField == null) {
            return null;
        }

        Object worldPass = null;
        if (pass != null) {
            worldPass = invokeObject(null, getWorldRenderPassMethod, pass);
        }
        if (worldPass == null) {
            try {
                worldPass = currentWorldRenderPassField != null ? currentWorldRenderPassField.get(null) : null;
            } catch (Throwable ignored) {
                worldPass = null;
            }
        }
        if (worldPass == null) {
            return null;
        }

        try {
            Object target = worldRenderPassTargetField.get(worldPass);
            return target instanceof RenderTarget renderTarget ? renderTarget : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static @Nullable RenderTarget getVrRendererEyeTarget(int eye) {
        if (eye < 0 || clientDataHolderGetInstanceMethod == null || vrRendererField == null || framebufferEye0Field == null || framebufferEye1Field == null) {
            return null;
        }

        Object holder = invokeObject(null, clientDataHolderGetInstanceMethod);
        if (holder == null) {
            return null;
        }

        try {
            Object renderer = vrRendererField.get(holder);
            if (renderer == null) {
                return null;
            }

            Object target = (eye == LEFT_EYE ? framebufferEye0Field : framebufferEye1Field).get(renderer);
            return target instanceof RenderTarget renderTarget ? renderTarget : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * @return Whether Veil should isolate post-processing resources for the current eye.
     */
    public static boolean usesPerEyePostProcessing() {
        VeilClientConfig config = VeilClientConfig.get();
        return config.enableVrCompatibility && config.usePerEyePostProcessing && isRenderingVrEye();
    }

    /**
     * @return Effective VR post scale after applying the optional performance cap.
     */
    @ApiStatus.Internal
    public static float getVrPostQuality() {
        return getEffectiveVrQuality(VeilClientConfig.get().vrPostQuality, 0.6F);
    }

    /**
     * @return Effective VR bloom scale after applying the optional performance cap.
     */
    @ApiStatus.Internal
    public static float getVrBloomQuality() {
        return getEffectiveVrQuality(VeilClientConfig.get().vrBloomQuality, 0.35F);
    }

    /**
     * Disables VR eye mask state while Veil draws fullscreen/light buffers.
     */
    @ApiStatus.Internal
    public static void pushVrRenderState() {
        if (!usesPerEyePostProcessing()) {
            VR_GL_STATES.push(VrGlState.INACTIVE);
            return;
        }

        boolean stencil = glIsEnabled(GL_STENCIL_TEST);
        boolean scissor = glIsEnabled(GL_SCISSOR_TEST);
        VR_GL_STATES.push(new VrGlState(true, stencil, scissor));
        // Vivecraft can leave a hidden-area eye mask active while the world is rendering. Veil's post
        // and light passes are screen-space, so they must temporarily opt out and then restore the state.
        if (stencil) {
            glDisable(GL_STENCIL_TEST);
        }
        if (scissor) {
            glDisable(GL_SCISSOR_TEST);
        }
    }

    /**
     * Restores GL state saved by {@link #pushVrRenderState()}.
     */
    @ApiStatus.Internal
    public static void popVrRenderState() {
        if (VR_GL_STATES.isEmpty()) {
            return;
        }

        VrGlState state = VR_GL_STATES.pop();
        if (!state.active) {
            return;
        }

        if (state.stencil) {
            glEnable(GL_STENCIL_TEST);
        } else {
            glDisable(GL_STENCIL_TEST);
        }
        if (state.scissor) {
            glEnable(GL_SCISSOR_TEST);
        } else {
            glDisable(GL_SCISSOR_TEST);
        }
    }

    /**
     * Uploads VR-aware uniforms to a post shader if they exist.
     */
    @ApiStatus.Internal
    public static void applyPostUniforms(ShaderProgram shader, AdvancedFbo viewport) {
        boolean vr = isVrActive();

        shader.getUniformSafe("VeilIsVR").setInt(vr ? 1 : 0);
        shader.getUniformSafe("VeilEyeIndex").setInt(getActiveEyeIndex());
        shader.getUniformSafe("VeilViewportSize").setVector(viewport.getWidth(), viewport.getHeight());

        applyVrSafetyUniforms(shader, vr);
    }

    @ApiStatus.Internal
    public static void recordVrPostPassTime(long nanos) {
    }

    @ApiStatus.Internal
    public static void recordVrFramebufferResize(int eye) {
    }

    @ApiStatus.Internal
    public static void recordVrFramebufferCreation(int eye) {
    }

    @ApiStatus.Internal
    public static void recordVrBlitCopy() {
    }

    /**
     * Warns once if a post pass uses a framebuffer that Veil cannot prove is per-eye isolated.
     */
    @ApiStatus.Internal
    public static void warnIfSharedBuffer(ResourceLocation name, String usage) {
        if (!VeilClientConfig.get().logVrSharedBufferWarnings || !isRenderingVrEye() || isKnownPerEyeBuffer(name) || isIsolatedFramebuffer(name) || !WARNED_SHARED_BUFFERS.add(name)) {
            return;
        }

        Veil.LOGGER.warn("Post pass {} references framebuffer {} while VR is active. Veil cannot prove this buffer is per-eye isolated.", usage, name);
    }

    /**
     * @return Whether clearing this output should be skipped to preserve Vivecraft compositing alpha.
     */
    @ApiStatus.Internal
    public static boolean shouldSkipAlphaUnsafeClear(ResourceLocation name) {
        if (!isRenderingVrEye() || !VeilFramebuffers.MAIN.equals(name)) {
            return false;
        }

        if (WARNED_ALPHA_CLEARS.add(name)) {
            Veil.LOGGER.debug("Skipping post clear for {} while VR is active to preserve Vivecraft framebuffer alpha.", name);
        }
        return true;
    }

    @ApiStatus.Internal
    public static void endFrame() {
        for (Map<ResourceLocation, IsolatedFramebuffer> framebuffers : ISOLATED_FRAMEBUFFERS) {
            for (IsolatedFramebuffer framebuffer : framebuffers.values()) {
                framebuffer.clearIfUsed();
            }
        }
    }

    @ApiStatus.Internal
    public static void free() {
        for (EyeFramebuffer framebuffer : POST_FRAMEBUFFERS) {
            framebuffer.free();
        }
        for (Map<ResourceLocation, IsolatedFramebuffer> framebuffers : ISOLATED_FRAMEBUFFERS) {
            framebuffers.values().forEach(IsolatedFramebuffer::free);
            framebuffers.clear();
        }
        VR_GL_STATES.clear();
        WARNED_SHARED_BUFFERS.clear();
        WARNED_ALPHA_CLEARS.clear();
    }

    private static void applyVrSafetyUniforms(ShaderProgram shader, boolean vr) {
        VeilClientConfig config = VeilClientConfig.get();
        float motionBlurScale = vr && config.disableMotionBlurInVR ? 0.0F : 1.0F;
        float cameraShakeScale = vr ? 0.0F : 1.0F;
        float distortionScale = vr && config.reduceDistortionInVR ? 0.35F : 1.0F;

        shader.getUniformSafe("VeilMotionBlurScale").setFloat(motionBlurScale);
        shader.getUniformSafe("VeilCameraShakeScale").setFloat(cameraShakeScale);
        shader.getUniformSafe("VeilDistortionScale").setFloat(distortionScale);

        if (motionBlurScale == 0.0F) {
            shader.getUniformSafe("MotionBlurStrength").setFloat(0.0F);
            shader.getUniformSafe("MotionBlurAmount").setFloat(0.0F);
            shader.getUniformSafe("motionBlurStrength").setFloat(0.0F);
            shader.getUniformSafe("motionBlurAmount").setFloat(0.0F);
        }
        if (cameraShakeScale == 0.0F) {
            shader.getUniformSafe("CameraShakeStrength").setFloat(0.0F);
            shader.getUniformSafe("CameraShakeAmount").setFloat(0.0F);
            shader.getUniformSafe("cameraShakeStrength").setFloat(0.0F);
            shader.getUniformSafe("cameraShakeAmount").setFloat(0.0F);
        }
        if (distortionScale < 1.0F) {
            shader.getUniformSafe("DistortionStrength").setFloat(distortionScale);
            shader.getUniformSafe("DistortionAmount").setFloat(distortionScale);
            shader.getUniformSafe("distortionStrength").setFloat(distortionScale);
            shader.getUniformSafe("distortionAmount").setFloat(distortionScale);
        }
    }

    private static boolean isKnownPerEyeBuffer(ResourceLocation name) {
        return "temp".equals(name.getNamespace()) ||
                VeilFramebuffers.MAIN.equals(name) ||
                Veil.veilPath("dynamic_main").equals(name) ||
                VeilFramebuffers.BLOOM.equals(name) ||
                VeilFramebuffers.POST.equals(name) ||
                VeilFramebuffers.TRANSLUCENT_TARGET.equals(name) ||
                VeilFramebuffers.ITEM_ENTITY_TARGET.equals(name) ||
                VeilFramebuffers.PARTICLES_TARGET.equals(name) ||
                VeilFramebuffers.WEATHER_TARGET.equals(name) ||
                VeilFramebuffers.CLOUDS_TARGET.equals(name);
    }

    private static boolean isIsolatedFramebuffer(ResourceLocation name) {
        int eye = getActiveEyeIndex();
        return eye >= 0 && ISOLATED_FRAMEBUFFERS[eye].containsKey(name);
    }

    private static void logVrStatus() {
        String device = getVrDeviceName();
        if (!loggedVrStatus || vivecraftDetected != lastStatusDetected || cachedVrInitialized != lastStatusInitialized || cachedVrActive != lastStatusActive || !device.equals(lastStatusDevice)) {
            loggedVrStatus = true;
            lastStatusDetected = vivecraftDetected;
            lastStatusInitialized = cachedVrInitialized;
            lastStatusActive = cachedVrActive;
            lastStatusDevice = device;
            Veil.LOGGER.info("Veil VR status: Vivecraft={}, initialized={}, active={}, device={}",
                    yesNo(vivecraftDetected),
                    yesNo(cachedVrInitialized),
                    yesNo(cachedVrActive),
                    device);
        }
    }

    private static String getVrDeviceName() {
        Object holder = invokeObject(null, clientDataHolderGetInstanceMethod);
        Object vr = null;
        if (holder != null && vrField != null) {
            try {
                vr = vrField.get(holder);
            } catch (Throwable ignored) {
                vr = null;
            }
        }
        if (vr == null) {
            return "unknown";
        }

        String provider = stringOrUnknown(invokeObject(vr, getVrProviderNameMethod));
        String runtime = stringOrUnknown(invokeObject(vr, getVrRuntimeNameMethod));
        String hardware = "unknown";
        if (detectedHardwareField != null) {
            try {
                Object value = detectedHardwareField.get(vr);
                if (value != null) {
                    hardware = String.valueOf(value);
                }
            } catch (Throwable ignored) {
                hardware = "unknown";
            }
        }

        if ("unknown".equals(runtime) && "unknown".equals(hardware)) {
            return provider;
        }
        return provider + " / runtime=" + runtime + " / hardware=" + hardware;
    }

    private static String stringOrUnknown(@Nullable Object value) {
        return value != null ? String.valueOf(value) : "unknown";
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }

    private static String getEyeName(int eye) {
        return switch (eye) {
            case LEFT_EYE -> "LEFT";
            case RIGHT_EYE -> "RIGHT";
            default -> "NONE";
        };
    }

    private static int getPostWidth(RenderTarget target) {
        return scaleVrTarget(target.width, getVrPostQuality());
    }

    private static int getPostHeight(RenderTarget target) {
        return scaleVrTarget(target.height, getVrPostQuality());
    }

    private static int getIsolatedWidth(ResourceLocation name, RenderTarget target) {
        return scaleVrTarget(target.width, getIsolatedQuality(name));
    }

    private static int getIsolatedHeight(ResourceLocation name, RenderTarget target) {
        return scaleVrTarget(target.height, getIsolatedQuality(name));
    }

    private static float getIsolatedQuality(ResourceLocation name) {
        return VeilFramebuffers.LIGHT.equals(name) ? getVrLightQuality() : getVrPostQuality();
    }

    private static float getVrLightQuality() {
        return getEffectiveVrQuality(VeilClientConfig.get().vrLightQuality, 0.5F);
    }

    private static float getEffectiveVrQuality(float configuredQuality, float performanceCap) {
        float quality = Math.max(0.25F, Math.min(1.0F, configuredQuality));
        if (VeilClientConfig.get().optimizeVrPerformance) {
            quality = Math.min(quality, performanceCap);
        }
        return quality;
    }

    private static int scaleVrTarget(int size, float quality) {
        return Math.max(1, Math.round(size * quality));
    }

    private static @Nullable Boolean invokeBoolean(@Nullable Object target, @Nullable Method method) {
        Object value = invokeObject(target, method);
        return value instanceof Boolean booleanValue ? booleanValue : null;
    }

    private static @Nullable Object invokeObject(@Nullable Object target, @Nullable Method method, Object... args) {
        if (method == null) {
            return null;
        }

        try {
            return method.invoke(target, args);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static final class EyeFramebuffer {

        private final String name;
        private AdvancedFbo framebuffer;
        private int width = -1;
        private int height = -1;

        private EyeFramebuffer(String name) {
            this.name = name;
        }

        private AdvancedFbo get(RenderTarget target) {
            int nextWidth = getPostWidth(target);
            int nextHeight = getPostHeight(target);
            if (this.framebuffer == null || this.width != nextWidth || this.height != nextHeight) {
                if (this.framebuffer != null) {
                    recordVrFramebufferResize("Right".equals(this.name) ? RIGHT_EYE : LEFT_EYE);
                }
                this.free();
                this.width = nextWidth;
                this.height = nextHeight;
                this.framebuffer = AdvancedFbo.withSize(nextWidth, nextHeight)
                        .setDebugLabel("Veil VR Post " + this.name)
                        .setFormat(FramebufferAttachmentDefinition.Format.RGB16F)
                        .addColorTextureBuffer()
                        .build(true);
                recordVrFramebufferCreation("Right".equals(this.name) ? RIGHT_EYE : LEFT_EYE);
            }
            return this.framebuffer;
        }

        private void free() {
            if (this.framebuffer != null) {
                this.framebuffer.free();
                this.framebuffer = null;
            }
            this.width = -1;
            this.height = -1;
        }
    }

    private static final class IsolatedFramebuffer {

        private final FramebufferDefinition definition;
        private final AdvancedFbo framebuffer;
        private final int width;
        private final int height;
        private boolean usedThisFrame;

        private IsolatedFramebuffer(FramebufferDefinition definition, AdvancedFbo framebuffer, int width, int height) {
            this.definition = definition;
            this.framebuffer = framebuffer;
            this.width = width;
            this.height = height;
        }

        private boolean matches(FramebufferDefinition definition, int width, int height) {
            return this.definition == definition && this.width == width && this.height == height;
        }

        private void markUsed() {
            this.usedThisFrame = true;
        }

        private void clearIfUsed() {
            if (this.usedThisFrame && this.definition.autoClear()) {
                this.framebuffer.clear();
            }
            this.usedThisFrame = false;
        }

        private void free() {
            this.framebuffer.free();
        }
    }

    private record VrGlState(boolean active, boolean stencil, boolean scissor) {

        private static final VrGlState INACTIVE = new VrGlState(false, false, false);
    }
}
