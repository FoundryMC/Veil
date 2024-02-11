package foundry.veil.api.client.render.deferred.light;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.impl.client.render.deferred.light.VanillaLightRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;

import java.util.*;
import java.util.function.Consumer;

/**
 * Renders all lights in a scene.
 * <p>Lights can be added with {@link #addLight(Light)}, and subsequently removed with
 * {@link #removeLight(Light)}. Lights are automatically updated the next time {@link #render(CullFrustum, AdvancedFbo)}
 * is called if {@link Light#isDirty()} is <code>true</code>.
 * </p>
 * <p>There is no way to retrieve a light, so care should be taken to keep track of what lights
 * have been added to the scene and when they should be removed.</p>
 *
 * @author Ocelot
 */
public class LightRenderer implements NativeResource {

    private final Map<Light.Type, LightData<?>> lights;

    private VanillaLightRenderer vanillaLightRenderer;
    private boolean vanillaLightEnabled;
    private boolean ambientOcclusionEnabled;
    private AdvancedFbo framebuffer;

    /**
     * Creates a new light renderer.
     */
    public LightRenderer() {
        this.lights = new EnumMap<>(Light.Type.class);
        this.vanillaLightEnabled = true;
        this.ambientOcclusionEnabled = true;
    }

    /**
     * Applies the shader set to {@link VeilRenderSystem}.
     */
    public void applyShader() {
        ShaderProgram shader = VeilRenderSystem.getShader();
        if (shader == null) {
            VeilRenderSystem.throwShaderError();
            return;
        }

        shader.bind();
        if (this.framebuffer != null) {
            shader.setFramebufferSamplers(this.framebuffer);
            shader.setVector("ScreenSize", this.framebuffer.getWidth(), this.framebuffer.getHeight());
        } else {
            shader.setVector("ScreenSize", 1.0F, 1.0F);
        }
        shader.applyShaderSamplers(0);
    }

    /**
     * Renders all lights into the light framebuffer.
     *
     * @param frustum     The frustum to cull lights with
     * @param framebuffer The framebuffer to sample from
     */
    public void render(CullFrustum frustum, AdvancedFbo framebuffer) {
        this.framebuffer = framebuffer;
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        RenderSystem.depthMask(false);

        for (LightData<?> value : this.lights.values()) {
            value.render(this, frustum);
        }
        if (this.vanillaLightEnabled) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                if (this.vanillaLightRenderer == null) {
                    this.vanillaLightRenderer = new VanillaLightRenderer();
                }
                this.vanillaLightRenderer.render(this, level);
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        this.framebuffer = null;
    }

    /**
     * Adds a light to the renderer.
     *
     * @param light The light to add
     */
    public void addLight(Light light) {
        Objects.requireNonNull(light, "light");
        RenderSystem.assertOnRenderThreadOrInit();
        this.lights.computeIfAbsent(light.getType(), LightData::new).addLight(light);
    }

    /**
     * Removes the specified light from the renderer.
     *
     * @param light The light to remove
     */
    public void removeLight(Light light) {
        Objects.requireNonNull(light, "light");
        RenderSystem.assertOnRenderThreadOrInit();

        LightData<?> data = this.lights.get(light.getType());
        if (data == null) {
            return;
        }

        if (data.lights.remove(light) && data.lights.isEmpty()) {
            data.free();
            this.lights.remove(light.getType());
        }
    }

    /**
     * Retrieves all lights of the specified type.
     *
     * @param type The type of lights to get
     * @return A list of all lights of the specified type in the scene
     */
    @SuppressWarnings("unchecked")
    public <T extends Light> List<T> getLights(Light.Type type) {
        LightData<?> data = this.lights.get(type);
        if (data == null) {
            return Collections.emptyList();
        }

        return (List<T>) data.lightsView;
    }

    /**
     * Enables the vanilla lightmap and directional shading.
     */
    public void enableVanillaLight() {
        this.vanillaLightEnabled = true;
    }

    /**
     * Disables the vanilla lightmap and directional shading.
     */
    public void disableVanillaLight() {
        this.vanillaLightEnabled = false;
        if (this.vanillaLightRenderer != null) {
            this.vanillaLightRenderer.free();
            this.vanillaLightRenderer = null;
        }
    }

    /**
     * Enables ambient occlusion.
     */
    public void enableAmbientOcclusion() {
        if (!this.ambientOcclusionEnabled) {
            this.ambientOcclusionEnabled = true;
            Minecraft.getInstance().levelRenderer.allChanged();
        }
    }

    /**
     * Disables ambient occlusion.
     */
    public void disableAmbientOcclusion() {
        if (this.ambientOcclusionEnabled) {
            this.ambientOcclusionEnabled = false;
            Minecraft.getInstance().levelRenderer.allChanged();
        }
    }

    /**
     * @return The deferred framebuffer being read from
     */
    public @Nullable AdvancedFbo getFramebuffer() {
        return this.framebuffer;
    }

    /**
     * @return Whether the vanilla lighting is enabled
     */
    public boolean isVanillaLightEnabled() {
        return this.vanillaLightEnabled;
    }

    /**
     * @return Whether chunks can have ambient occlusion
     */
    public boolean isAmbientOcclusionEnabled() {
        return this.ambientOcclusionEnabled;
    }

    @Override
    public void free() {
        this.lights.values().forEach(LightData::free);
        this.lights.clear();
        if (this.vanillaLightRenderer != null) {
            this.vanillaLightRenderer.free();
            this.vanillaLightRenderer = null;
        }
    }

    @ApiStatus.Internal
    public void addDebugInfo(Consumer<String> consumer) {
        CullFrustum frustum = VeilRenderer.getCullingFrustum();
        int visible = this.lights.values().stream().mapToInt(data -> (int) data.lights.stream().filter(light -> light.isVisible(frustum)).count()).sum();
        int all = this.lights.values().stream().mapToInt(data -> data.lights.size()).sum();
        consumer.accept("Lights: " + visible + " / " + all);
    }

    @ApiStatus.Internal
    private static class LightData<T extends Light> implements NativeResource {

        private final LightTypeRenderer<T> renderer;
        private final List<T> lights;
        private final List<T> lightsView;
        private final Set<T> removedLights;

        private LightData(LightTypeRenderer<T> renderer) {
            this.renderer = renderer;
            this.lights = new ArrayList<>();
            this.lightsView = Collections.unmodifiableList(this.lights);
            this.removedLights = new HashSet<>();
        }

        @SuppressWarnings("unchecked")
        public LightData(Light.Type type) {
            this((LightTypeRenderer<T>) Objects.requireNonNull(type, "type").createRenderer());
        }

        private void render(LightRenderer lightRenderer, CullFrustum frustum) {
            this.lights.removeAll(this.removedLights);
            this.renderer.renderLights(lightRenderer, this.lights, this.removedLights, frustum);
            this.removedLights.clear();
        }

        @SuppressWarnings("unchecked")
        private void addLight(Light light) {
            this.lights.add((T) light);
        }

        @Override
        public void free() {
            this.renderer.free();
        }
    }
}
