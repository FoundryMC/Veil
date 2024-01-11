package foundry.veil.render.deferred.light;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.render.deferred.LightRenderer;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.shader.VeilShaders;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33C.glVertexAttribDivisor;

/**
 * Renders point lights.
 *
 * @author Ocelot
 */
public class PointLightRenderer extends InstancedLightRenderer<PointLight> {

    public PointLightRenderer() {
        super(100, Float.BYTES * 8);
    }

    @Override
    protected @NotNull BufferBuilder.RenderedBuffer createMesh() {
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

    @Override
    protected void setupBufferState() {
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, this.lightSize, 0);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, this.lightSize, Float.BYTES * 3);
        glVertexAttribPointer(3, 1, GL_FLOAT, false, this.lightSize, Float.BYTES * 6);
        glVertexAttribPointer(4, 1, GL_FLOAT, false, this.lightSize, Float.BYTES * 7);

        glVertexAttribDivisor(1, 1);
        glVertexAttribDivisor(2, 1);
        glVertexAttribDivisor(3, 1);
        glVertexAttribDivisor(4, 1);
    }

    @Override
    protected void setupRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<PointLight> lights) {
        VeilRenderSystem.setShader(VeilShaders.LIGHT_POINT);
    }

    @Override
    protected void clearRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<PointLight> lights) {
    }
}
