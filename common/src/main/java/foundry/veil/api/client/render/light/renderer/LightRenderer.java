package foundry.veil.api.client.render.light.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.Veil;
import foundry.veil.api.client.registry.LightTypeRegistry;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.dynamicbuffer.DynamicBufferType;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.light.data.LightData;
import foundry.veil.api.client.render.vertex.VertexArray;
import foundry.veil.impl.client.render.light.VoxelShadowGrid;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;
import org.lwjgl.system.NativeResource;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11C.*;

/**
 * Renders all lights in a scene.
 * <br>
 * There is no way to retrieve a light, so care should be taken to keep track of what lights
 * have been added to the scene and when they should be removed.
 *
 * @author Ocelot
 */
public final class LightRenderer implements NativeResource {

    private static final ResourceLocation BUFFER_ID = Veil.veilPath("lights");
    private final Map<LightTypeRegistry.LightType<?>, LightTypeRenderer<?>> renderers;
    private final Map<LightTypeRegistry.LightType<?>, LightTypeRenderer<?>> renderersView;

    /**
     * Creates a new light renderer.
     */
    public LightRenderer() {
        this.renderers = new Object2ObjectArrayMap<>();
        this.renderersView = Collections.unmodifiableMap(this.renderers);
    }

    /**
     * Draws the lights to the specified framebuffer.
     *
     * @param lightFbo The framebuffer to render lights into
     * @return If any lights were actually rendered
     */
    @ApiStatus.Internal
    public boolean render(CullFrustum frustum, AdvancedFbo lightFbo) {
        boolean hasRendered = false;
        boolean setupDDA = false;
        VeilRenderer renderer = VeilRenderSystem.renderer();

        for (LightTypeRenderer<?> lightRenderer : this.renderers.values()) {
            lightRenderer.prepareLights(this, frustum);

            // If there are no visible lights, then don't render anything
            if (lightRenderer.getVisibleLights() <= 0) {
                continue;
            }

            if (!hasRendered) {
                if (renderer.enableBuffers(BUFFER_ID, DynamicBufferType.ALBEDO, DynamicBufferType.NORMAL)) {
                    return false;
                }

                lightFbo.bind(true);
                lightFbo.clear(GL_COLOR_BUFFER_BIT);
                AdvancedFbo.getMainFramebuffer().resolveToAdvancedFbo(lightFbo, GL_DEPTH_BUFFER_BIT, GL_NEAREST);
            }

            hasRendered = true;

            // Decide if the DDA needs to be updated
            if (lightRenderer instanceof DDALightRenderer<?> ddalightRenderer) {
                if (!setupDDA) {
                    VoxelShadowGrid.setup();
                    setupDDA = true;
                }
                ddalightRenderer.uploadVoxelGridUniforms(VoxelShadowGrid.getTextureId(), VoxelShadowGrid.getUniformGridPos());
            }
            lightRenderer.renderLights(this);
        }

        if (!hasRendered) {
            renderer.disableBuffers(BUFFER_ID, DynamicBufferType.ALBEDO, DynamicBufferType.NORMAL);
            return false;
        }

        VertexArray.unbind();
        return true;
    }

    /**
     * Adds a light to the renderer.
     *
     * @param lightData The light to add
     */
    @SuppressWarnings("unchecked")
    public <T extends LightData> LightRenderHandle<T> addLight(T lightData) {
        Objects.requireNonNull(lightData, "light");
        RenderSystem.assertOnRenderThreadOrInit();
        return ((LightTypeRenderer<T>) this.renderers.computeIfAbsent(lightData.getType(), lightType -> lightType.rendererFactory().createRenderer())).addLight(lightData);
    }

    /**
     * Attempts to re-add the specified light handle to the renderer.
     *
     * @param handle The handle of the light to add
     * @return The same handle or a new one if re-added
     */
    @SuppressWarnings("unchecked")
    public <T extends LightData> LightRenderHandle<T> addLight(LightRenderHandle<T> handle) {
        Objects.requireNonNull(handle, "light");
        RenderSystem.assertOnRenderThreadOrInit();
        return ((LightTypeRenderer<T>) this.renderers.computeIfAbsent(handle.getLightData().getType(), lightType -> lightType.rendererFactory().createRenderer())).steal(handle);
    }

    /**
     * Retrieves all lights of the specified type.
     *
     * @param type The type of lights to get
     * @return A list of lights for the specified type in the scene
     */
    @SuppressWarnings("unchecked")
    public <T extends LightData> Collection<? extends LightRenderHandle<T>> getLights(LightTypeRegistry.LightType<? extends T> type) {
        LightTypeRenderer<?> renderer = this.renderers.get(type);
        return renderer != null ? (Collection<? extends LightRenderHandle<T>>) renderer.getLights() : Collections.emptyList();
    }

    /**
     * @return A view of all existing light renderers
     * @since 3.3.0
     */
    @UnmodifiableView
    public Map<LightTypeRegistry.LightType<?>, LightTypeRenderer<?>> getRenderers() {
        return this.renderersView;
    }

    @Override
    public void free() {
        this.renderers.values().forEach(LightTypeRenderer::free);
        this.renderers.clear();
    }

    @ApiStatus.Internal
    public void addDebugInfo(Consumer<String> consumer) {
        int visible = 0;
        int all = 0;
        for (LightTypeRenderer<?> renderer : this.renderers.values()) {
            visible += renderer.getVisibleLights();
            all += renderer.getLights().size();
        }
        consumer.accept("Lights: " + visible + " / " + all);
    }
}
