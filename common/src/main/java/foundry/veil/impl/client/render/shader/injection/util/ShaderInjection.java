package foundry.veil.impl.client.render.shader.injection.util;

import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

@ApiStatus.Internal
public interface ShaderInjection {

    void inject(GlslTree tree, boolean applyVersion) throws GlslSyntaxException, IOException;

    int priority();
}
