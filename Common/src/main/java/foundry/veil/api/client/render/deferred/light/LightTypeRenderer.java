package foundry.veil.api.client.render.deferred.light;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.CullFrustum;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

import java.util.List;
import java.util.Set;

/**
 * Renders all lights of a specific type.
 *
 * @param <T> The type of lights to render
 */
public interface LightTypeRenderer<T extends Light> extends NativeResource {

    /**
     * <p>Renders all specified lights with this renderer.</p>
     * <p>Shaders, custom uniforms, and the way lights are rendered is up to the individual renderer.</p>
     *
     * @param lightRenderer The light renderer instance
     * @param lights        The lights to render
     * @param removedLights The lights that will be removed this frame
     * @param frustum       The culling view frustum
     */
    @ApiStatus.OverrideOnly
    void renderLights(LightRenderer lightRenderer, List<T> lights, Set<T> removedLights, CullFrustum frustum);

    /**
     * @return A full-screen unit quad for drawing a light
     */
    static BufferBuilder.RenderedBuffer createQuad() {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(-1, -1, 0).endVertex();
        bufferBuilder.vertex(1, -1, 0).endVertex();
        bufferBuilder.vertex(-1, 1, 0).endVertex();
        bufferBuilder.vertex(1, 1, 0).endVertex();

        return bufferBuilder.end();
    }

    /**
     * @return A full-screen unit quad for drawing a light
     */
    static BufferBuilder.RenderedBuffer createInvertedCube() {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(-1, 1, 1).endVertex(); // Front-top-left
        bufferBuilder.vertex(1, 1, 1).endVertex(); // Front-top-right
        bufferBuilder.vertex(-1, -1, 1).endVertex(); // Front-bottom-left
        bufferBuilder.vertex(1, -1, 1).endVertex(); // Front-bottom-right
        bufferBuilder.vertex(1, -1, -1).endVertex(); // Back-bottom-right
        bufferBuilder.vertex(1, 1, 1).endVertex(); // Front-top-right
        bufferBuilder.vertex(1, 1, -1).endVertex(); // Back-top-right
        bufferBuilder.vertex(-1, 1, 1).endVertex(); // Front-top-left
        bufferBuilder.vertex(-1, 1, -1).endVertex(); // Back-top-left
        bufferBuilder.vertex(-1, -1, 1).endVertex(); // Front-bottom-left
        bufferBuilder.vertex(-1, -1, -1).endVertex(); // Back-bottom-left
        bufferBuilder.vertex(1, -1, -1).endVertex(); // Back-bottom-right
        bufferBuilder.vertex(-1, 1, -1).endVertex(); // Back-top-left
        bufferBuilder.vertex(1, 1, -1).endVertex(); // Back-top-right

        return bufferBuilder.end();
    }
}
