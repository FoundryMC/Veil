package foundry.veil.impl.client.render.deferred.light;

import com.mojang.blaze3d.vertex.BufferBuilder;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.deferred.light.AreaLight;
import foundry.veil.api.client.render.deferred.light.InstancedLightRenderer;
import foundry.veil.api.client.render.deferred.light.LightRenderer;
import foundry.veil.api.client.render.deferred.light.LightTypeRenderer;
import foundry.veil.api.client.render.shader.VeilShaders;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33C.glVertexAttribDivisor;

@ApiStatus.Internal
public class AreaLightRenderer extends InstancedLightRenderer<AreaLight> {

    public AreaLightRenderer() {
        super(Float.BYTES * 22 + 2);
    }

    @Override
    protected @NotNull BufferBuilder.RenderedBuffer createMesh() {
        return LightTypeRenderer.createInvertedCube();
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

        glVertexAttribPointer(1, 4, GL_FLOAT, false, this.lightSize, 0);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, this.lightSize, Float.BYTES * 4);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, this.lightSize, Float.BYTES * 8);
        glVertexAttribPointer(4, 4, GL_FLOAT, false, this.lightSize, Float.BYTES * 12); // matrix !

        glVertexAttribPointer(5, 3, GL_FLOAT, false, this.lightSize, Float.BYTES * 16); // color
        glVertexAttribPointer(6, 2, GL_FLOAT, false, this.lightSize, Float.BYTES * 19); // size
        glVertexAttribPointer(7, 1, GL_UNSIGNED_SHORT, true, this.lightSize, Float.BYTES * 21); // angle
        glVertexAttribPointer(8, 1, GL_FLOAT, false, this.lightSize, Float.BYTES * 21 + 2); // distance

        glVertexAttribDivisor(1, 1);
        glVertexAttribDivisor(2, 1);
        glVertexAttribDivisor(3, 1);
        glVertexAttribDivisor(4, 1);
        glVertexAttribDivisor(5, 1);
        glVertexAttribDivisor(6, 1);
        glVertexAttribDivisor(7, 1);
        glVertexAttribDivisor(8, 1);
    }

    @Override
    protected void setupRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<AreaLight> lights) {
        VeilRenderSystem.setShader(VeilShaders.LIGHT_AREA);
    }

    @Override
    protected void clearRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<AreaLight> lights) {
    }
}
