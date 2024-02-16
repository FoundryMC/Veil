package foundry.veil.impl.client.render.deferred.light;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.deferred.VeilDeferredRenderer;
import foundry.veil.api.client.render.deferred.light.LightRenderer;
import foundry.veil.api.client.render.deferred.light.LightTypeRenderer;
import foundry.veil.api.client.render.framebuffer.VeilFramebuffers;
import foundry.veil.api.client.render.shader.VeilShaders;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

@ApiStatus.Internal
public class VanillaLightRenderer implements NativeResource {

    private final VertexBuffer vbo;

    public VanillaLightRenderer() {
        this.vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.vbo.bind();
        this.vbo.upload(createMesh());
        VertexBuffer.unbind();
    }

    private static BufferBuilder.RenderedBuffer createMesh() {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);
        LightTypeRenderer.createQuad(bufferBuilder);
        return bufferBuilder.end();
    }

    public void render(LightRenderer lightRenderer, ClientLevel level) {
        VeilRenderer renderer = VeilRenderSystem.renderer();
        boolean useBaked = renderer.getShaderDefinitions().getDefinition(VeilDeferredRenderer.USE_BAKED_TRANSPARENT_LIGHTMAPS_KEY) != null && lightRenderer.getFramebuffer() == renderer.getFramebufferManager().getFramebuffer(VeilFramebuffers.TRANSPARENT);
        VeilRenderSystem.setShader(useBaked ? VeilShaders.LIGHT_VANILLA : VeilShaders.LIGHT_VANILLA_LIGHTMAP);
        lightRenderer.applyShader();

        ShaderProgram shader = VeilRenderSystem.getShader();
        if (shader == null) {
            return;
        }

        for (Direction direction : Direction.values()) {
            shader.setFloat("LightShading" + direction.ordinal(), level.getShade(direction, true));
        }

        this.vbo.bind();
        this.vbo.draw();
        VertexBuffer.unbind();
    }

    @Override
    public void free() {
        this.vbo.close();
    }
}
