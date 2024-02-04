package foundry.veil.impl.client.render.deferred.light;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.deferred.light.AreaLight;
import foundry.veil.api.client.render.deferred.light.InstancedLightRenderer;
import foundry.veil.api.client.render.deferred.light.LightRenderer;
import foundry.veil.api.client.render.deferred.light.PointLight;
import foundry.veil.api.client.render.shader.VeilShaders;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL33C.glVertexAttribDivisor;

@ApiStatus.Internal
public class AreaLightRenderer extends InstancedLightRenderer<AreaLight> {
    public AreaLightRenderer() {
        super(100, Float.BYTES * 24);
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
        glEnableVertexAttribArray(5);
        glEnableVertexAttribArray(6);
        glEnableVertexAttribArray(7);
        glEnableVertexAttribArray(8);
        glEnableVertexAttribArray(9);

        glVertexAttribPointer(1,4, GL_FLOAT, false, this.lightSize, 0);
        glVertexAttribPointer(2,4, GL_FLOAT, false, this.lightSize, Float.BYTES * 4);
        glVertexAttribPointer(3,4, GL_FLOAT, false, this.lightSize, Float.BYTES * 8);
        glVertexAttribPointer(4,4, GL_FLOAT, false, this.lightSize, Float.BYTES * 12); // matrix !

        glVertexAttribPointer(5, 3, GL_FLOAT, false, this.lightSize, Float.BYTES * 16); // color
        glVertexAttribPointer(6, 2, GL_FLOAT, false, this.lightSize, Float.BYTES * 19); // size
        glVertexAttribPointer(7, 1, GL_FLOAT, false, this.lightSize, Float.BYTES * 21); // angle
        glVertexAttribPointer(8, 1, GL_FLOAT, false, this.lightSize, Float.BYTES * 22); // distance
        glVertexAttribPointer(9, 1, GL_FLOAT, false, this.lightSize, Float.BYTES * 23); // falloff

        glVertexAttribDivisor(1, 1);
        glVertexAttribDivisor(2, 1);
        glVertexAttribDivisor(3, 1);
        glVertexAttribDivisor(4, 1);
        glVertexAttribDivisor(5, 1);
        glVertexAttribDivisor(6, 1);
        glVertexAttribDivisor(7, 1);
        glVertexAttribDivisor(8, 1);
        glVertexAttribDivisor(9, 1);
    }

    @Override
    protected void setupRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<AreaLight> lights) {
        VeilRenderSystem.setShader(VeilShaders.LIGHT_AREA);
    }

    @Override
    protected void clearRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<AreaLight> lights) {
    }
}
