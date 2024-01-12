package foundry.veil.render.wrapper;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.render.deferred.VeilDeferredRenderer;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.shader.program.ShaderProgram;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ApiStatus.Internal
public class DeferredShaderStateCache {

    private ShaderInstance veil$oldShader;
    private ShaderInstance veil$deferredShader;

    /**
     * Sets up the render state for the specified shader instance.
     *
     * @param shaderInstance The shader to set the render state for
     * @return Whether the state was handled and set
     */
    public boolean setupRenderState(@Nullable ShaderInstance shaderInstance) {
        VeilDeferredRenderer deferredRenderer = VeilRenderSystem.renderer().getDeferredRenderer();
        if (shaderInstance == null || !deferredRenderer.isActive()) {
            this.veil$oldShader = null;
            this.veil$deferredShader = null;
            return false;
        }

        if (!Objects.equals(this.veil$oldShader, shaderInstance)) {
            this.veil$oldShader = shaderInstance;
            ShaderProgram deferredShader = deferredRenderer.getDeferredShaderManager().getShader(new ResourceLocation(shaderInstance.getName()));
            this.veil$deferredShader = deferredShader != null ? deferredShader.toShaderInstance() : null;
        }
        if (this.veil$deferredShader != null) {
            RenderSystem.setShader(() -> this.veil$deferredShader);
            return true;
        }

        return false;
    }
}
