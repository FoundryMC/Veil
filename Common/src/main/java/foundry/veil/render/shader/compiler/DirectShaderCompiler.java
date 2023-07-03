package foundry.veil.render.shader.compiler;

import foundry.veil.render.shader.ShaderException;
import foundry.veil.render.shader.ShaderManager;
import foundry.veil.render.shader.definition.ShaderPreDefinitions;
import foundry.veil.render.shader.processor.*;
import foundry.veil.render.shader.program.ProgramDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL20C.*;

/**
 * Creates a new shader and compiles each time {@link #compile(ShaderCompiler.Context, int, String)} is called.
 * This should only be used for compiling single shaders.
 *
 * @author Ocelot
 */
@ApiStatus.Internal
public class DirectShaderCompiler implements ShaderCompiler {

    private static final boolean VERBOSE_ERRORS;

    static {
        VERBOSE_ERRORS = System.getProperty("veil.verboseShaderErrors") != null;
    }

    private final ResourceProvider provider;
    private final List<ShaderPreProcessor> preProcessors;
    private final List<ShaderPreProcessor> importProcessors;
    private final Set<Integer> shaders;
    private ResourceLocation compilingName;

    DirectShaderCompiler(@Nullable ResourceProvider provider) {
        this.provider = provider;
        this.preProcessors = new LinkedList<>();
        this.importProcessors = new LinkedList<>();
        this.shaders = new HashSet<>();
    }

    private String modifySource(ShaderCompiler.Context context, List<ShaderPreProcessor> preProcessors, Map<String, Integer> uniformBindings, Set<String> dependencies, @Nullable ResourceLocation name, String source, int type, boolean sourceFile) throws IOException {
        for (ShaderPreProcessor preProcessor : preProcessors) {
            source = preProcessor.modify(new PreProcessorContext(this, context, uniformBindings, dependencies, name, source, type, sourceFile));
        }
        return source;
    }

    @Override
    public CompiledShader compile(ShaderCompiler.Context context, int type, ResourceLocation id) throws IOException, ShaderException {
        if (this.provider == null) {
            throw new IOException("Failed to read " + ShaderManager.getTypeName(type) + " from " + id + " because no provider was specified");
        }

        ResourceLocation location = ShaderManager.getTypeConverter(type).idToFile(id);
        try (Reader reader = this.provider.openAsReader(location)) {
            this.compilingName = id;
            return this.compile(context, type, IOUtils.toString(reader));
        } finally {
            this.compilingName = null;
        }
    }

    @Override
    public CompiledShader compile(ShaderCompiler.Context context, int type, String source) throws IOException, ShaderException {
        this.preProcessors.forEach(ShaderPreProcessor::prepare);

        Map<String, Integer> uniformBindings = new HashMap<>();
        Set<String> dependencies = new HashSet<>();
        source = this.modifySource(context, this.preProcessors, uniformBindings, dependencies, this.compilingName, source, type, true);

        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE) {
            String log = glGetShaderInfoLog(shader);
            if (DirectShaderCompiler.VERBOSE_ERRORS) {
                log += "\n" + source;
            }
            glDeleteShader(shader); // Delete to prevent leaks
            throw new ShaderException("Failed to compile " + ShaderManager.getTypeName(type) + " shader", log);
        }

        this.shaders.add(shader);
        return new CompiledShader(shader, uniformBindings, dependencies);
    }

    @Override
    public void addPreprocessor(ShaderPreProcessor processor, boolean modifyImports) {
        this.preProcessors.add(processor);
        if (modifyImports) {
            this.importProcessors.add(processor);
        }
    }

    @Override
    public ShaderCompiler addDefaultProcessors() {
        if (this.provider != null) {
            this.addPreprocessor(new ShaderImportProcessor(this.provider));
        }
        this.addPreprocessor(new ShaderBindingProcessor());
        this.addPreprocessor(new ShaderPredefinitionProcessor(), false);
        this.addPreprocessor(new ShaderVersionProcessor(), false);
        return this;
    }

    @Override
    public void free() {
        this.preProcessors.clear();
        this.shaders.forEach(GL20C::glDeleteShader);
        this.shaders.clear();
    }

    private record PreProcessorContext(DirectShaderCompiler compiler,
                                       ShaderCompiler.Context context,
                                       Map<String, Integer> uniformBindings,
                                       Set<String> dependencies,
                                       @Nullable ResourceLocation name,
                                       String input,
                                       int type,
                                       boolean sourceFile) implements ShaderPreProcessor.Context {

        @Override
        public String modify(@Nullable ResourceLocation name, String source) throws IOException {
            return this.compiler.modifySource(this.context, this.compiler.importProcessors, this.uniformBindings, this.dependencies, name, source, this.type, false);
        }

        @Override
        public void addUniformBinding(String name, int binding) {
            this.uniformBindings.put(name, binding);
        }

        @Override
        public void addDefinitionDependency(String name) {
            this.dependencies.add(name);
        }

        @Override
        public @Nullable ResourceLocation getName() {
            return this.name;
        }

        @Override
        public String getInput() {
            return this.input;
        }

        @Override
        public int getType() {
            return this.type;
        }

        @Override
        public boolean isSourceFile() {
            return this.sourceFile;
        }

        @Override
        public @Nullable ProgramDefinition getDefinition() {
            return this.context.definition();
        }

        @Override
        public ShaderPreDefinitions getPreDefinitions() {
            return this.context.preDefinitions();
        }
    }
}
