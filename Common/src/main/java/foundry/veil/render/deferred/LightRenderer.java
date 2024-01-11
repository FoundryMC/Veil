package foundry.veil.render.deferred;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.render.deferred.light.DirectionalLight;
import foundry.veil.render.deferred.light.Light;
import foundry.veil.render.deferred.light.LightTypeRenderer;
import foundry.veil.render.framebuffer.AdvancedFbo;
import foundry.veil.render.framebuffer.FramebufferManager;
import foundry.veil.render.framebuffer.VeilFramebuffers;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.shader.program.ShaderProgram;
import foundry.veil.render.wrapper.CullFrustum;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;

import java.util.*;
import java.util.function.Consumer;

/**
 * Renders all lights in a scene.
 * <p>Lights can be added with {@link #addLight(Light)}, and subsequently removed with
 * {@link #removeLight(Light)}. Lights are automatically updated the next time {@link #render(CullFrustum)}
 * is called if {@link Light#isDirty()} is <code>true</code>.
 * </p>
 * <p>There is no way to retrieve a light, so care should be taken to keep track of what lights
 * have been added to the scene and when they should be removed.</p>
 *
 * @author Ocelot
 */
public class LightRenderer implements NativeResource {

    private final FramebufferManager framebufferManager;
    private final Map<Light.Type, LightData<?>> lights;

    private DirectionalLight mainLight;

    /**
     * Creates a new light renderer.
     *
     * @param framebufferManager The manager to retrieve the deferred and light framebuffers from
     */
    public LightRenderer(FramebufferManager framebufferManager) {
        this.framebufferManager = framebufferManager;
        this.lights = new EnumMap<>(Light.Type.class);
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

        AdvancedFbo deferredBuffer = this.framebufferManager.getFramebuffer(VeilFramebuffers.DEFERRED);

        shader.bind();
        if (deferredBuffer != null) {
            shader.setFramebufferSamplers(deferredBuffer);
            shader.setVector("ScreenSize", deferredBuffer.getWidth(), deferredBuffer.getHeight());
        } else {
            shader.setVector("ScreenSize", 1.0F, 1.0F);
        }
        shader.applyShaderSamplers(0);
    }

    /**
     * Renders all lights into the light framebuffer.
     *
     * @param frustum The frustum to cull lights with
     */
    public void render(CullFrustum frustum) {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        this.lights.values().forEach(data -> data.render(this, frustum));

        RenderSystem.disableBlend();
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
        if (this.mainLight == null && light.getType() == Light.Type.DIRECTIONAL && light instanceof DirectionalLight directionalLight) {
            this.mainLight = directionalLight;
        }
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
    public List<? extends Light> getLights(Light.Type type) {
        LightData<?> data = this.lights.get(type);
        if (data == null) {
            return Collections.emptyList();
        }

        return data.lightsView();
    }

    /**
     * @return The framebuffer manager instance
     */
    public FramebufferManager getFramebufferManager() {
        return this.framebufferManager;
    }

    /**
     * @return The main light in the scene or <code>null</code> if there is no primary directional light
     */
    public @Nullable DirectionalLight getMainLight() {
        return this.mainLight;
    }

    @Override
    public void free() {
        this.lights.values().forEach(LightData::free);
        this.lights.clear();
    }

    @ApiStatus.Internal
    public void addDebugInfo(Consumer<String> consumer) {
        consumer.accept("Lights: " + this.lights.values().stream().mapToInt(data -> data.lights().size()).sum());
    }

    @ApiStatus.Internal
    private record LightData<T extends Light>(LightTypeRenderer<T> renderer,
                                              List<T> lights,
                                              List<T> lightsView,
                                              List<T> visibleLights) implements NativeResource {

        private LightData {
            Objects.requireNonNull(renderer, "renderer");
            Objects.requireNonNull(lights, "lights");
            Objects.requireNonNull(lightsView, "lightsView");
        }

        private LightData(LightTypeRenderer<T> renderer, List<T> lights) {
            this(Objects.requireNonNull(renderer, "renderer"),
                    Objects.requireNonNull(lights, "lights"),
                    Collections.unmodifiableList(lights),
                    new LinkedList<>());
        }

        @SuppressWarnings("unchecked")
        public LightData(Light.Type type) {
            this((LightTypeRenderer<T>) Objects.requireNonNull(type, "type").createRenderer(), new LinkedList<>());
        }

        private void render(LightRenderer lightRenderer, CullFrustum frustum) {
            this.visibleLights.clear();
            for (T light : this.lights) {
                if (light.isVisible(frustum)) {
                    this.visibleLights.add(light);
                }
            }

            this.renderer.renderLights(lightRenderer, this.visibleLights);
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
