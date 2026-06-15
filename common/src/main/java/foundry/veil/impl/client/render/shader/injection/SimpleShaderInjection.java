package foundry.veil.impl.client.render.shader.injection;

import foundry.veil.impl.client.render.shader.injection.util.ShaderInjection;
import foundry.veil.impl.client.render.shader.injection.util.ShaderInjectionFunction;
import io.github.ocelot.glslprocessor.api.GlslParser;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.grammar.GlslVersionStatement;
import io.github.ocelot.glslprocessor.api.node.GlslNode;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.api.node.function.GlslFunctionNode;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

@ApiStatus.Internal
public class SimpleShaderInjection implements ShaderInjection {

    private final int version;
    private final int priority;
    private final ShaderInjectionFunction[] functions;
    private final String globals;

    public SimpleShaderInjection(int version, int priority, ShaderInjectionFunction[] functions) {
        this(version, priority, functions, null);
    }

    public SimpleShaderInjection(int version, int priority, ShaderInjectionFunction[] functions, @Nullable String globals) {
        this.version = version;
        this.priority = priority;
        this.functions = functions;
        this.globals = globals;
    }

    @Override
    public void inject(GlslTree tree, boolean applyVersion) throws GlslSyntaxException, IOException {
        if (applyVersion && this.version >= 0) {
            GlslVersionStatement version = tree.getVersionStatement();
            if (version.getVersion() < this.version) {
                version.setVersion(this.version);
            }
        }

        if (this.globals != null && !this.globals.isEmpty()) {
            tree.getBody().addAll(0, GlslParser.parse(this.globals).getBody());
        }

        for (ShaderInjectionFunction function : this.functions) {
            String name = function.name();
            List<GlslNode> body = tree.functions().filter(definition -> {
                        if (definition == null || definition.getBody() == null) {
                            return false;
                        }
                        if (!name.equals(definition.getName())) {
                            return false;
                        }
                        int paramCount = function.parameters();
                        return paramCount == -1 || definition.getHeader().getParameters().size() == paramCount;
                    })
                    .findFirst()
                    .map(GlslFunctionNode::getBody)
                    .orElseThrow(() -> {
                        int paramCount = function.parameters();
                        if (paramCount == -1) {
                            return new IOException("Unknown function: " + name);
                        }
                        return new IOException("Unknown function with " + paramCount + " parameters: " + name);
                    });

            for (GlslNode node : GlslParser.parseExpressionList(function.code())) {
                if (function.head()) {
                    body.addFirst(node);
                } else {
                    body.add(node);
                }
            }
        }
    }

    @Override
    public int priority() {
        return this.priority;
    }

    public ShaderInjectionFunction[] getShaderInjectionFunctions() {
        return this.functions;
    }
}
