package foundry.veil.render.wrapper;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.render.deferred.VeilDeferredRenderer;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.shader.program.ShaderProgram;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
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
        ShaderInstance shader = this.getShader(shaderInstance);
        if (shader != shaderInstance) {
            RenderSystem.setShader(() -> shader);
            return true;
        }
        return false;
    }

    /**
     * Retrieves the shader that should be used if using defered rendering
     *
     * @param shaderInstance The shader to get the render state for
     * @return The shader to use
     */
    @Contract("null -> null")
    public ShaderInstance getShader(ShaderInstance shaderInstance) {
        VeilDeferredRenderer deferredRenderer = VeilRenderSystem.renderer().getDeferredRenderer();
        if (shaderInstance == null || !deferredRenderer.isActive()) {
            this.veil$oldShader = null;
            this.veil$deferredShader = null;
            return shaderInstance;
        }

        if (!Objects.equals(this.veil$oldShader, shaderInstance)) {
            this.veil$oldShader = shaderInstance;
            ShaderProgram deferredShader = deferredRenderer.getDeferredShaderManager().getShader(new ResourceLocation(shaderInstance.getName()));
            this.veil$deferredShader = deferredShader != null ? deferredShader.toShaderInstance() : null;
        }
        return Objects.requireNonNullElse(this.veil$deferredShader, shaderInstance);

    }
}
