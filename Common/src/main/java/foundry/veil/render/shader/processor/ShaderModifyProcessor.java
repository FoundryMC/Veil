package foundry.veil.render.shader.processor;

import foundry.veil.render.shader.ShaderModificationManager;

import java.io.IOException;

/**
 * Modifies shader sources with the shader modification system.
 *
 * @author Ocelot
 */
public class ShaderModifyProcessor implements ShaderPreProcessor {

    private final ShaderModificationManager shaderModificationManager;

    public ShaderModifyProcessor(ShaderModificationManager shaderModificationManager) {
        this.shaderModificationManager = shaderModificationManager;
    }

    @Override
    public String modify(Context context) throws IOException {
        return context.getInput();
    }
}
