package foundry.veil.render.shader;

import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.pipeline.VeilRenderer;
import foundry.veil.render.shader.definition.ShaderPreDefinitions;
import foundry.veil.render.shader.processor.ShaderImportProcessor;
import foundry.veil.render.shader.processor.ShaderPreProcessor;
import foundry.veil.render.shader.program.ProgramDefinition;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Allows vanilla shaders to use <code>#define namespace:id</code> imports
 */
@ApiStatus.Internal
public class VanillaShaderImportProcessor {

    private static ShaderImportProcessor processor;

    public static void setup(ResourceProvider resourceProvider) {
        processor = new ShaderImportProcessor(resourceProvider);
    }

    public static void free() {
        processor = null;
    }

    public static String modify(String source) throws IOException {
        if (processor == null) {
            throw new NullPointerException("Processor not initialized");
        }
        return processor.modify(new Context(source));
    }

    private record Context(String source) implements ShaderPreProcessor.Context {

        @Override
        public String modify(@Nullable ResourceLocation name, String source) throws IOException {
            return VanillaShaderImportProcessor.modify(source);
        }

        @Override
        public void addUniformBinding(String name, int binding) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addDefinitionDependency(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable ResourceLocation getName() {
            return null;
        }

        @Override
        public String getInput() {
            return this.source;
        }

        @Override
        public int getType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileToIdConverter getConverter() {
            return VeilRenderSystem.renderer().getShaderManager().getSourceSet().getTypeConverter(this.getType());
        }

        @Override
        public boolean isSourceFile() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable ProgramDefinition getDefinition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ShaderPreDefinitions getPreDefinitions() {
            throw new UnsupportedOperationException();
        }
    }
}
