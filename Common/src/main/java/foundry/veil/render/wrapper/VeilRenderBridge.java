package foundry.veil.render.wrapper;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import foundry.veil.render.framebuffer.AdvancedFbo;
import foundry.veil.render.shader.ShaderManager;
import foundry.veil.render.shader.program.ShaderProgram;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.function.Supplier;

/**
 * Bridges between Minecraft and Veil render classes.
 *
 * @author Ocelot
 */
public interface VeilRenderBridge {

    /**
     * Wraps the specified render target in a new advanced fbo.
     *
     * @param renderTarget The render target instance
     * @return A new advanced fbo that wraps the target in the api
     */
    static AdvancedFbo wrap(RenderTarget renderTarget) {
        return VeilRenderBridge.wrap(() -> renderTarget);
    }

    /**
     * Wraps the specified render target in a new advanced fbo.
     *
     * @param renderTargetSupplier The supplier to the render target instance
     * @return A new advanced fbo that wraps the target in the api
     */
    static AdvancedFbo wrap(Supplier<RenderTarget> renderTargetSupplier) {
        return new VanillaAdvancedFboWrapper(renderTargetSupplier);
    }
}
