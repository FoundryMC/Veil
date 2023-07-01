package foundry.veil.render.shader.compiler;

import foundry.veil.render.shader.ShaderException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Attempts to cache the exact same shader sources to reduce the number of compiled shaders.
 *
 * @author Ocelot
 */
@ApiStatus.Internal
public class CachedShaderCompiler extends DirectShaderCompiler {

    private final Map<Integer, CompiledShader> shaders;

    CachedShaderCompiler(@Nullable ResourceProvider provider) {
        super(provider);
        this.shaders = new HashMap<>();
    }

    @Override
    public CompiledShader compile(ShaderCompiler.Context context, int type, ResourceLocation id) throws IOException, ShaderException {
        int hash = Objects.hash(type, id);
        if (this.shaders.containsKey(hash)) {
            return this.shaders.get(hash);
        }
        CompiledShader shader = super.compile(context, type, id);
        this.shaders.put(hash, shader);
        return shader;
    }

    @Override
    public CompiledShader compile(ShaderCompiler.Context context, int type, String source) throws IOException, ShaderException {
        int hash = Objects.hash(type, source);
        if (this.shaders.containsKey(hash)) {
            return this.shaders.get(hash);
        }
        CompiledShader shader = super.compile(context, type, source);
        this.shaders.put(hash, shader);
        return shader;
    }

    @Override
    public void free() {
        super.free();
        this.shaders.clear();
    }
}
