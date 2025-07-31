package foundry.veil.api.client.render.light.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.light.data.LightData;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

import java.util.Collection;

/**
 * Renders all lights of a specific type.
 *
 * @param <T> The type of lights to render
 * @since 2.0.0
 */
public interface LightTypeRenderer<T extends LightData> extends NativeResource {

    /**
     * Adds the specified light to the renderer.
     *
     * @param light The light to add
     * @return A handle to the light in the renderer
     */
    LightRenderHandle<T> addLight(T light);

    /**
     * Attempts to re-add the specified handle to this renderer if invalid.
     *
     * @param handle The handle to add
     * @return A valid handle to this renderer
     */
    LightRenderHandle<T> steal(LightRenderHandle<T> handle);

    /**
     * Prepares the light renderer.
     *
     * @param lightRenderer The light renderer instance
     * @param frustum       The culling view frustum
     */
    @ApiStatus.OverrideOnly
    void prepareLights(LightRenderer lightRenderer, CullFrustum frustum);

    /**
     * Renders all prepared lights with this renderer.
     * <br>
     * Shaders, custom uniforms, and the way lights are rendered is up to the individual renderer.
     *
     * @param lightRenderer The light renderer instance
     */
    void renderLights(LightRenderer lightRenderer);

    /**
     * @return A view of all lights in this renderer
     */
    Collection<? extends LightRenderHandle<T>> getLights();

    /**
     * @return The number of lights visible last frame
     */
    int getVisibleLights();

    /**
     * Draws a unit quad into the specified buffer
     *
     * @param builder The builder to draw into
     */
    static void createQuad(VertexConsumer builder) {
        builder.addVertex(-1, -1, 0);
        builder.addVertex(1, -1, 0);
        builder.addVertex(-1, 1, 0);
        builder.addVertex(1, 1, 0);
    }

    /**
     * Draws a unit inverted cube into the specified buffer
     *
     * @param builder The builder to draw into
     */
    static void createInvertedCube(VertexConsumer builder) {
        builder.addVertex(-1, 1, 1); // Front-top-left
        builder.addVertex(1, 1, 1); // Front-top-right
        builder.addVertex(-1, -1, 1); // Front-bottom-left
        builder.addVertex(1, -1, 1); // Front-bottom-right
        builder.addVertex(1, -1, -1); // Back-bottom-right
        builder.addVertex(1, 1, 1); // Front-top-right
        builder.addVertex(1, 1, -1); // Back-top-right
        builder.addVertex(-1, 1, 1); // Front-top-left
        builder.addVertex(-1, 1, -1); // Back-top-left
        builder.addVertex(-1, -1, 1); // Front-bottom-left
        builder.addVertex(-1, -1, -1); // Back-bottom-left
        builder.addVertex(1, -1, -1); // Back-bottom-right
        builder.addVertex(-1, 1, -1); // Back-top-left
        builder.addVertex(1, 1, -1); // Back-top-right
    }
}
