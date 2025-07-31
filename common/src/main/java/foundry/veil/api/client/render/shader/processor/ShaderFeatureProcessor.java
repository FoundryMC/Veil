package foundry.veil.api.client.render.shader.processor;

import foundry.veil.api.client.render.shader.ShaderFeature;
import foundry.veil.api.client.render.shader.program.ProgramDefinition;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.lib.anarres.cpp.LexerException;

import java.io.IOException;

/**
 * Applies required shader modifications when using the {@link ShaderFeature} API.
 *
 * @author Ocelot
 * @since 2.0.0
 */
public class ShaderFeatureProcessor implements ShaderPreProcessor {

    @Override
    public void modify(Context ctx, GlslTree tree) throws IOException, GlslSyntaxException, LexerException {
        if (!(ctx instanceof VeilContext veilContext)) {
            return;
        }

        ProgramDefinition definition = veilContext.definition();
        if (definition == null) {
            return;
        }

        for (ShaderFeature requiredFeature : definition.requiredFeatures()) {
            requiredFeature.modifyShader(tree);
        }
    }
}
