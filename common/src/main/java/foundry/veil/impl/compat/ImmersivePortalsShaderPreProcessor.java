package foundry.veil.impl.compat;

import com.mojang.blaze3d.shaders.Program;
import foundry.veil.api.client.render.shader.processor.ShaderPreProcessor;
import foundry.veil.api.compat.ImmersivePortalsCompat;
import io.github.ocelot.glslprocessor.api.GlslParser;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.lib.anarres.cpp.LexerException;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

@ApiStatus.Internal
public class ImmersivePortalsShaderPreProcessor implements ShaderPreProcessor {

    @Override
    public void modify(Context ctx, GlslTree tree) throws IOException, GlslSyntaxException, LexerException {
        ImmersivePortalsCompat compat = ImmersivePortalsCompat.INSTANCE;
        if (ctx.name() == null || compat == null) return;

        Program.Type type;
        if (ctx.type() == GL_VERTEX_SHADER) {
            type = Program.Type.VERTEX;
        } else if (ctx.type() == GL_FRAGMENT_SHADER) {
            type = Program.Type.FRAGMENT;
        } else {
            return;
        }

        String key = ctx.name().toString();
        if (!compat.shouldAddUniform(key)) {
            key = ctx.name().getPath();
            if (!compat.shouldAddUniform(key)) {
                key = key.substring(key.lastIndexOf('/') + 1, key.length() - 4);
                if (!compat.shouldAddUniform(key)) {
                    return;
                }
            }
        }

        String transformed = compat.transform(type, key, tree.toSourceString());
        GlslTree newShader = GlslParser.parse(transformed);

        tree.getBody().clear();
        tree.getBody().addAll(newShader.getBody());
    }
}
