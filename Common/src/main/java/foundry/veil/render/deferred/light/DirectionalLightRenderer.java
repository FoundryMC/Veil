package foundry.veil.render.deferred.light;

import com.mojang.blaze3d.vertex.VertexBuffer;
import foundry.veil.render.deferred.LightRenderer;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.shader.VeilShaders;
import foundry.veil.render.shader.program.ShaderProgram;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Renders directional lights.
 *
 * @author Ocelot
 */
public class DirectionalLightRenderer implements LightTypeRenderer<DirectionalLight> {

    private final VertexBuffer vbo;

    public DirectionalLightRenderer() {
        this.vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.vbo.bind();
        this.vbo.upload(LightTypeRenderer.createQuad());
        VertexBuffer.unbind();
    }

    @Override
    public void renderLights(@NotNull LightRenderer lightRenderer, @NotNull List<DirectionalLight> lights) {
        VeilRenderSystem.setShader(VeilShaders.LIGHT_DIRECTIONAL);
        lightRenderer.applyShader();

        ShaderProgram shader = VeilRenderSystem.getShader();
        if (shader == null) {
            return;
        }

        this.vbo.bind();
        for (DirectionalLight light : lights) {
            shader.setVector("LightColor", light.getColor());
            shader.setVector("LightDirection", light.getDirection());
            this.vbo.draw();
        }

        VertexBuffer.unbind();
    }

    @Override
    public void free() {
        this.vbo.close();
    }
}
