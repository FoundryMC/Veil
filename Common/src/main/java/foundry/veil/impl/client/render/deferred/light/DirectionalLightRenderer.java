package foundry.veil.impl.client.render.deferred.light;

import com.mojang.blaze3d.vertex.VertexBuffer;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.deferred.light.DirectionalLight;
import foundry.veil.api.client.render.deferred.light.LightRenderer;
import foundry.veil.api.client.render.deferred.light.LightTypeRenderer;
import foundry.veil.api.client.render.shader.VeilShaders;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Set;

@ApiStatus.Internal
public class DirectionalLightRenderer implements LightTypeRenderer<DirectionalLight> {

    private final VertexBuffer vbo;

    public DirectionalLightRenderer() {
        this.vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.vbo.bind();
        this.vbo.upload(LightTypeRenderer.createQuad());
        VertexBuffer.unbind();
    }

    @Override
    public void renderLights(LightRenderer lightRenderer, List<DirectionalLight> lights, Set<DirectionalLight> removedLights, CullFrustum frustum) {
        if (lights.isEmpty()) {
            return;
        }

        VeilRenderSystem.setShader(VeilShaders.LIGHT_DIRECTIONAL);
        lightRenderer.applyShader();

        ShaderProgram shader = VeilRenderSystem.getShader();
        if (shader == null) {
            return;
        }

        this.vbo.bind();
        for (DirectionalLight light : lights) {
            Vector3fc lightColor = light.getColor();
            float brightness = light.getBrightness();
            shader.setVector("LightColor", lightColor.x() * brightness, lightColor.y() * brightness, lightColor.z() * brightness);
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
