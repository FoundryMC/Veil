package foundry.veil.render.deferred.light.renderer;

import com.mojang.blaze3d.vertex.VertexBuffer;
import foundry.veil.render.deferred.LightRenderer;
import foundry.veil.render.deferred.VeilDeferredRenderer;
import foundry.veil.render.framebuffer.AdvancedFbo;
import foundry.veil.render.framebuffer.VeilFramebuffers;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.pipeline.VeilRenderer;
import foundry.veil.render.shader.VeilShaders;
import foundry.veil.render.shader.program.ShaderProgram;
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
        this.vbo.upload(LightTypeRenderer.createQuad());
        VertexBuffer.unbind();
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
