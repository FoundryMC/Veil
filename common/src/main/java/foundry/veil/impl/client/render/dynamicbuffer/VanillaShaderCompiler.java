package foundry.veil.impl.client.render.dynamicbuffer;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.compat.ImmersivePortalsCompat;
import foundry.veil.ext.ShaderInstanceExtension;
import foundry.veil.impl.ThreadTaskScheduler;
import foundry.veil.impl.client.render.shader.processor.VanillaShaderProcessor;
import foundry.veil.impl.client.render.shader.program.ShaderProgramImpl;
import net.minecraft.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;

/**
 * Compiles vanilla shaders asynchronously.
 */
public class VanillaShaderCompiler {

    private static final Set<String> LAST_FRAME_SHADERS = ConcurrentHashMap.newKeySet();

    private ThreadTaskScheduler scheduler;

    public VanillaShaderCompiler() {
    }

    private void compileShader(ShaderInstance shader, int activeBuffers, GLCapabilities glCapabilities) {
        ShaderInstanceExtension extension = (ShaderInstanceExtension) shader;
        Collection<ResourceLocation> shaderSources = extension.veil$getShaderSources();
        VertexFormat vertexFormat = shader.getVertexFormat();
        Map<String, Object> customProgramData = new HashMap<>();
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        VanillaShaderProcessor.setup(resourceManager);
        for (ResourceLocation path : shaderSources) {
            try (Reader reader = resourceManager.openAsReader(path)) {
                String source = IOUtils.toString(reader);
                GlslPreprocessor preprocessor = new GlslPreprocessor() {
                    private final Set<ResourceLocation> importedPaths = new HashSet<>();

                    @Override
                    public String applyImport(boolean useFullPath, @NotNull String directory) {
                        ResourceLocation loc = ResourceLocation.parse(directory);
                        String normalised = FileUtil.normalizeResourcePath((useFullPath ? path : "shaders/include/") + loc.getPath());
                        ResourceLocation resourcelocation = ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), normalised);

                        if (!this.importedPaths.add(resourcelocation)) {
                            return null;
                        }

                        try (Reader reader = resourceManager.openAsReader(resourcelocation)) {
                            return IOUtils.toString(reader);
                        } catch (IOException e) {
                            throw new RuntimeException("Could not open GLSL import " + resourcelocation, e);
                        }
                    }
                };
                source = String.join("", preprocessor.process(source));

                boolean vertex = path.getPath().endsWith(".vsh");
                String processed = VanillaShaderProcessor.modify(customProgramData, shader.getName(), path, vertexFormat, activeBuffers, vertex ? GL_VERTEX_SHADER : GL_FRAGMENT_SHADER, source, glCapabilities);
                Minecraft.getInstance().execute(() -> {
                    extension.veil$recompile(vertex, processed, activeBuffers);
                    VeilRenderSystem.renderer().getDynamicBufferManger().markRecompiled(shader);
                });
            } catch (Throwable t) {
                Veil.LOGGER.error("Couldn't load vanilla shader from {}, skipping", path, t);
            }
        }
        VanillaShaderProcessor.free();
    }

    /**
     * Attempts to preload all vanilla minecraft shader files before creating the shaders on the CPU.
     *
     * @param shaders The shaders to reload
     * @return A future for when vanilla shaders have reloaded
     */
    public CompletableFuture<?> reload(Collection<ShaderInstance> shaders) {
        if (this.scheduler != null) {
            // Cancel the previous tasks and move on
            this.scheduler.cancel();
        }

        int shaderCount = shaders.size();
        Map<String, ShaderInstance> shaderMap = new ConcurrentHashMap<>(shaderCount);
        for (ShaderInstance shader : shaders) {
            // Don't try to recompile wrappers here
            if (!(shader instanceof ShaderProgramImpl.Wrapper)) {
                shaderMap.put(shader.getName(), shader);
            }
        }

        int activeBuffers = VeilRenderSystem.renderer().getDynamicBufferManger().getActiveBuffers();
        GLCapabilities capabilities = GL.getCapabilities();

        if (ImmersivePortalsCompat.isLoaded()) {
            ImmersivePortalsCompat.INSTANCE.init();
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        ThreadTaskScheduler scheduler = new ThreadTaskScheduler("VeilVanillaShaderCompile", Math.max(1, Runtime.getRuntime().availableProcessors() / 6), () -> {
            for (String lastFrameShader : LAST_FRAME_SHADERS) {
                ShaderInstance shader = shaderMap.remove(lastFrameShader);
                if (shader != null) {
                    return () -> this.compileShader(shader, activeBuffers, capabilities);
                }
            }

            Iterator<ShaderInstance> iterator = shaderMap.values().iterator();
            if (iterator.hasNext()) {
                ShaderInstance shader = iterator.next();
                iterator.remove();
                return () -> this.compileShader(shader, activeBuffers, capabilities);
            }
            return null;
        });
        this.scheduler = scheduler;

        CompletableFuture<?> future = scheduler.getCompletedFuture();
        future.thenRunAsync(() -> {
            if (!scheduler.isCancelled()) {
                Veil.LOGGER.info("Compiled {} vanilla shaders in {}", shaderCount, stopwatch.stop());
            }
        }, Minecraft.getInstance());
        return future.isDone() ? CompletableFuture.completedFuture(null) : future;
    }

    /**
     * @return Whether shaders are currently being rendered
     */
    public boolean isCompilingShaders() {
        return this.scheduler != null && !this.scheduler.getCompletedFuture().isDone();
    }

    /**
     * Retrieves the active buffers for an existing shader program.
     *
     * @return The active buffers in the specified program
     * @since 2.3.0
     */
    public static int getActiveDynamicBuffers(ShaderInstance shaderInstance) {
        if (shaderInstance instanceof ShaderProgramImpl.Wrapper wrapper) {
            return wrapper.program().getActiveDynamicBuffers();
        }
        return ((ShaderInstanceExtension) shaderInstance).veil$getActiveBuffers();
    }

    @ApiStatus.Internal
    public static void markRendered(String shaderInstace) {
        if (VeilRenderSystem.renderer().getVanillaShaderCompiler().isCompilingShaders()) {
            LAST_FRAME_SHADERS.add(shaderInstace);
        }
    }

    @ApiStatus.Internal
    public static void clear() {
        LAST_FRAME_SHADERS.clear();
    }
}
