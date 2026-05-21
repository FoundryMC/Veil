package foundry.veil.api.client.render.post.stage;

import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import foundry.veil.api.compat.VeilVRCompat;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * An abstract stage that uses a framebuffer as the input and output.
 *
 * @author Ocelot
 */
public abstract class FramebufferPostStage implements PostPipeline {

    private final ResourceLocation in;
    private final ResourceLocation out;
    private final boolean clear;

    /**
     * Creates a post stage with the specified input and output framebuffers.
     *
     * @param in    The framebuffer to use as <code>DiffuseSampler0</code>-<code>DiffuseSampler...max</code>
     *              and <code>DiffuseDepthSampler</code>
     * @param out   The framebuffer to write into
     * @param clear Whether to clear the output before drawing
     */
    public FramebufferPostStage(@Nullable ResourceLocation in, ResourceLocation out, boolean clear) {
        this.in = in;
        this.out = out;
        this.clear = clear;
    }

    /**
     * Applies the input framebuffer textures and binds the output framebuffer.
     *
     * @param context The context for post-processing
     * @param shader  The shader to set input samplers to
     */
    protected void setupFramebuffer(Context context, ShaderProgram shader) {
        AdvancedFbo in = this.in != null ? context.getFramebuffer(this.in) : null;
        AdvancedFbo out = context.getFramebufferOrDraw(this.out);

        if (this.in != null) {
            VeilVRCompat.warnIfSharedBuffer(this.in, "input");
        }
        VeilVRCompat.warnIfSharedBuffer(this.out, "output");

        if (in != null) {
            shader.setFramebufferSamplers(in);
        }

        out.bind(true);
        if (this.clear && !VeilVRCompat.shouldSkipAlphaUnsafeClear(this.out)) {
            out.clear();
        }

        ShaderUniform inSize = shader.getUniform("InSize");
        if (inSize != null) {
            if (in != null) {
                inSize.setVector(in.getWidth(), in.getHeight());
            } else {
                inSize.setVector(1.0F, 1.0F);
            }
        }

        ShaderUniform outSize = shader.getUniform("OutSize");
        if (outSize != null) {
            outSize.setVector(out.getWidth(), out.getHeight());
        }
    }

    /**
     * @return The framebuffer to read from
     */
    public @Nullable ResourceLocation getIn() {
        return this.in;
    }

    /**
     * @return The framebuffer to write into
     */
    public ResourceLocation getOut() {
        return this.out;
    }

    /**
     * @return Whether the output should be cleared before drawing
     */
    public boolean clearOut() {
        return this.clear;
    }
}
