package foundry.veil.impl.client.render.shader.processor;

import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.dynamicbuffer.DynamicBufferType;
import foundry.veil.api.client.render.shader.processor.ShaderImporter;
import foundry.veil.api.client.render.shader.processor.ShaderInjectProcessor;
import foundry.veil.api.client.render.shader.processor.ShaderPreProcessor;
import foundry.veil.impl.client.render.dynamicbuffer.DynamicBufferProcessor;
import foundry.veil.impl.compat.sodium.SodiumShaderPreProcessor;
import io.github.ocelot.glslprocessor.api.GlslParser;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.lib.anarres.cpp.LexerException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows vanilla and sodium shaders to use shader modifications.
 */
@ApiStatus.Internal
public class SodiumShaderProcessor {

    private static final ThreadLocal<ShaderProcessorList> PROCESSOR = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Object>> CUSTOM_PROGRAM_DATA = new ThreadLocal<>();
    private static final ThreadLocal<Integer> SHADER_TYPE = new ThreadLocal<>();
    private static final ThreadLocal<ResourceLocation> SHADER_NAME = new ThreadLocal<>();
    private static final ThreadLocal<GLCapabilities> GL_CAPABILITIES = new ThreadLocal<>();

    public static void setShaderType(int type, ResourceLocation shaderName, GLCapabilities glCapabilities) {
        SHADER_TYPE.set(type);
        SHADER_NAME.set(shaderName);
        GL_CAPABILITIES.set(glCapabilities);
    }

    public static void setup(ResourceProvider provider) {
        ShaderProcessorList list = new ShaderProcessorList(provider);
        list.addPreprocessor(new ShaderInjectProcessor(), false);
        list.addPreprocessor(new DynamicBufferProcessor(), false);
        list.addPreprocessor(new SodiumShaderPreProcessor());
        VeilClient.clientPlatform().onRegisterShaderPreProcessors(provider, list);
        PROCESSOR.set(list);
        CUSTOM_PROGRAM_DATA.set(new HashMap<>());
    }

    public static void free() {
        PROCESSOR.remove();
        CUSTOM_PROGRAM_DATA.remove();
        SHADER_TYPE.remove();
        SHADER_NAME.remove();
        GL_CAPABILITIES.remove();
    }

    public static String modify(int activeBuffers, String source) throws IOException, GlslSyntaxException, LexerException {
        ResourceLocation shaderName = SHADER_NAME.get();
        if (shaderName == null) {
            return source;
        }

        ShaderProcessorList processor = PROCESSOR.get();
        if (processor == null) {
            throw new NullPointerException("Processor not initialized");
        }

        processor.getShaderImporter().reset();
        Map<String, String> macros = new HashMap<>();
        DynamicBufferType.addMacros(activeBuffers, macros);
        VeilRenderSystem.renderer().getShaderManager().addMacros(macros);
        GlslTree tree = GlslParser.preprocessParse(source, macros);
        processor.getProcessor().modify(new Context(CUSTOM_PROGRAM_DATA.get(), processor, shaderName.withPrefix("shaders/"), activeBuffers, SHADER_TYPE.get(), GL_CAPABILITIES.get(), macros, true), tree);
        GlslTree.stripGLMacros(macros);
        tree.getMacros().putAll(macros);
        return tree.toSourceString();
    }

    public static @Nullable ResourceLocation getActiveShaderName() {
        return SHADER_NAME.get();
    }

    private record Context(Map<String, Object> customProgramData,
                           ShaderProcessorList processor,
                           @Nullable ResourceLocation name,
                           int activeBuffers,
                           int type,
                           GLCapabilities glCapabilities,
                           Map<String, String> macros,
                           boolean sourceFile) implements ShaderPreProcessor.SodiumContext {

        @Override
        public GlslTree modifyInclude(@Nullable ResourceLocation name, String source) throws IOException, GlslSyntaxException, LexerException {
            GlslTree tree = GlslParser.preprocessParse(source, this.macros);
            this.processor.getImportProcessor().modify(new Context(this.customProgramData, this.processor, name, this.activeBuffers, this.type, this.glCapabilities, this.macros, false), tree);
            return tree;
        }

        @Override
        public boolean isSourceFile() {
            return this.sourceFile;
        }

        @Override
        public ShaderImporter shaderImporter() {
            return this.processor.getShaderImporter();
        }
    }
}
