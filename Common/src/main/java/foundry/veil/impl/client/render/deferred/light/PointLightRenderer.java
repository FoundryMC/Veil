package foundry.veil.impl.client.render.deferred.light;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.deferred.light.IndirectLightRenderer;
import foundry.veil.api.client.render.deferred.light.LightRenderer;
import foundry.veil.api.client.render.deferred.light.LightTypeRenderer;
import foundry.veil.api.client.render.deferred.light.PointLight;
import foundry.veil.api.client.render.shader.VeilShaders;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33C.glVertexAttribDivisor;

@ApiStatus.Internal
public class PointLightRenderer extends IndirectLightRenderer<PointLight> {

    public PointLightRenderer() {
        super(Float.BYTES * 7, 4, 0, 6);
    }

    @Override
    protected BufferBuilder.RenderedBuffer createMesh() {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);

        // High-res mesh
        LightTypeRenderer.createInvertedCube(bufferBuilder);

        // Low-res mesh
        float sqrt2 = (float) Math.sqrt(2.0);
        bufferBuilder.vertex(-sqrt2, -sqrt2, 0).endVertex();
        bufferBuilder.vertex(sqrt2, -sqrt2, 0).endVertex();
        bufferBuilder.vertex(-sqrt2, sqrt2, 0).endVertex();
        bufferBuilder.vertex(sqrt2, sqrt2, 0).endVertex();

        return bufferBuilder.end();
    }

    @Override
    protected void setupBufferState() {
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, this.lightSize, 0);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, this.lightSize, Float.BYTES * 3);
        glVertexAttribPointer(3, 1, GL_FLOAT, false, this.lightSize, Float.BYTES * 6);

        glVertexAttribDivisor(1, 1);
        glVertexAttribDivisor(2, 1);
        glVertexAttribDivisor(3, 1);
    }

    @Override
    protected void setupRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<PointLight> lights) {
        VeilRenderSystem.setShader(VeilShaders.LIGHT_POINT);
    }

    @Override
    protected void clearRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<PointLight> lights) {
    }
}
