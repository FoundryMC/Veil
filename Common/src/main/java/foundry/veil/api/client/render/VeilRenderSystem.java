package foundry.veil.api.client.render;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import foundry.veil.Veil;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.definition.ShaderBlock;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.opencl.VeilOpenCL;
import foundry.veil.impl.client.VeilImGuiImpl;
import foundry.veil.impl.client.render.pipeline.VeilUniformBlockState;
import foundry.veil.impl.client.render.shader.ShaderProgramImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.opengl.GL;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11C.glGetInteger;
import static org.lwjgl.opengl.GL30C.GL_MAX_COLOR_ATTACHMENTS;
import static org.lwjgl.opengl.GL31C.GL_MAX_UNIFORM_BUFFER_BINDINGS;
import static org.lwjgl.opengl.GL43C.GL_MAX_FRAMEBUFFER_HEIGHT;
import static org.lwjgl.opengl.GL43C.GL_MAX_FRAMEBUFFER_WIDTH;

/**
 * Additional functionality for {@link RenderSystem}.
 */
public final class VeilRenderSystem {

    private static final Executor RENDER_THREAD_EXECUTOR = task -> {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(task::run);
        } else {
            task.run();
        }
    };
    private static final Set<ResourceLocation> ERRORED_SHADERS = new HashSet<>();
    private static final VeilUniformBlockState UNIFORM_BLOCK_STATE = new VeilUniformBlockState();

    private static final IntSupplier MAX_COLOR_ATTACHMENTS = VeilRenderSystem.glGetter(() -> glGetInteger(GL_MAX_COLOR_ATTACHMENTS));
    private static final IntSupplier MAX_UNIFORM_BUFFER_BINDINGS = VeilRenderSystem.glGetter(() -> glGetInteger(GL_MAX_UNIFORM_BUFFER_BINDINGS));
    private static final Supplier<Vector2ic> MAX_FRAMEBUFFER_SIZE = Suppliers.memoize(() -> {
        RenderSystem.assertOnRenderThreadOrInit();
        boolean gl43 = GL.getCapabilities().OpenGL43;
        int width = gl43 ? glGetInteger(GL_MAX_FRAMEBUFFER_WIDTH) : Integer.MAX_VALUE;
        int height = gl43 ? glGetInteger(GL_MAX_FRAMEBUFFER_HEIGHT) : Integer.MAX_VALUE;
        return new Vector2i(width, height);
    });

    private static VeilRenderer renderer;
    private static ResourceLocation shaderLocation;

    private VeilRenderSystem() {
    }

    private static IntSupplier glGetter(IntSupplier delegate) {
        return new IntSupplier() {
            private int value = Integer.MAX_VALUE;

            @Override
            public int getAsInt() {
                RenderSystem.assertOnRenderThreadOrInit();
                if (this.value == Integer.MAX_VALUE) {
                    return this.value = delegate.getAsInt();
                }
                return this.value;
            }
        };
    }

    @ApiStatus.Internal
    public static void init() {
        Minecraft client = Minecraft.getInstance();
        if (!(client.getResourceManager() instanceof ReloadableResourceManager resourceManager)) {
            throw new IllegalStateException("Client resource manager is " + client.getResourceManager().getClass());
        }

        renderer = new VeilRenderer(resourceManager);
        VeilImGuiImpl.init(client.getWindow().getWindow());
    }

    /**
     * Sets the shader instance to be a reference to the shader manager.
     *
     * @param shader The name of the shader to use
     */
    public static void setShader(ResourceLocation shader) {
        ShaderManager shaderManager = renderer.getShaderManager();
        VeilRenderSystem.setShader(() -> shaderManager.getShader(shader));
        VeilRenderSystem.shaderLocation = shader;
    }

    /**
     * Sets the shader instance to a specific instance of a shader. {@link #setShader(ResourceLocation)} should be used in most cases.
     *
     * @param shader The shader instance to use
     */
    public static void setShader(@Nullable ShaderProgram shader) {
        VeilRenderSystem.setShader(() -> shader);
        VeilRenderSystem.shaderLocation = shader != null ? shader.getId() : null;
    }

    /**
     * Sets the shader instance to a specific instance reference of a shader. {@link #setShader(ResourceLocation)} should be used in most cases.
     *
     * @param shader The reference to the shader to use
     */
    public static void setShader(Supplier<ShaderProgram> shader) {
        RenderSystem.setShader(() -> {
            ShaderProgram program = shader.get();
            return program != null ? program.toShaderInstance() : null;
        });
    }

    /**
     * Clears all pending shader errors and re-queues uniform block ids to shaders.
     */
    public static void finalizeShaderCompilation() {
        ERRORED_SHADERS.clear();
        UNIFORM_BLOCK_STATE.queueUpload();
    }

    /**
     * Prints an error to console about the current shader.
     * This is useful to debug if a shader has an error while trying to be used.
     */
    public static void throwShaderError() {
        if (VeilRenderSystem.shaderLocation != null && ERRORED_SHADERS.add(VeilRenderSystem.shaderLocation)) {
            Veil.LOGGER.error("Failed to apply shader: " + VeilRenderSystem.shaderLocation);
        }
    }

    /**
     * @return The GL maximum amount of color attachments a framebuffer can have
     */
    public static int maxColorAttachments() {
        return VeilRenderSystem.MAX_COLOR_ATTACHMENTS.getAsInt();
    }

    /**
     * @return The GL maximum amount of uniform buffers bindings available
     */
    public static int maxUniformBuffersBindings() {
        return VeilRenderSystem.MAX_UNIFORM_BUFFER_BINDINGS.getAsInt();
    }

    /**
     * @return The GL maximum width of framebuffers
     */
    public static int maxFramebufferWidth() {
        return VeilRenderSystem.MAX_FRAMEBUFFER_SIZE.get().x();
    }

    /**
     * @return The GL maximum width of framebuffers
     */
    public static int maxFramebufferHeight() {
        return VeilRenderSystem.MAX_FRAMEBUFFER_SIZE.get().y();
    }

    /**
     * <p>Binds the specified block into the next available binding spot
     * and updates all shaders if the binding index has changed.</p>
     * <p><b>Make sure this is called before trying to use the block on this frame as it may have been overwritten.</b></p>
     *
     * @param block The block to bind
     */
    public static void bind(ShaderBlock<?> block) {
        RenderSystem.assertOnRenderThreadOrInit();
        UNIFORM_BLOCK_STATE.bind(block);
    }

    /**
     * <p>Binds the specified block into the next available binding spot
     * and updates all shaders if the binding index has changed.</p>
     * <p><b>Make sure this is called before trying to use the block on this frame as it may have been overwritten.</b></p>
     * <p>This binds the block and assigns it to shader values.</p>
     *
     * @param name  The name of the block in shader code
     * @param block The block to bind
     */
    public static void bind(CharSequence name, ShaderBlock<?> block) {
        RenderSystem.assertOnRenderThreadOrInit();
        UNIFORM_BLOCK_STATE.bind(name, block);
    }

    /**
     * Unbinds the specified block and frees the binding it occupied.
     * It isn't strictly necessary to unbind blocks, but they should not be referenced anymore after being deleted.
     *
     * @param block The block to unbind
     */
    public static void unbind(ShaderBlock<?> block) {
        RenderSystem.assertOnRenderThreadOrInit();
        UNIFORM_BLOCK_STATE.unbind(block);
    }

    /**
     * Binds the specified vertex array and invalidates the vanilla MC immediate buffer state.
     *
     * @param vao The vao to bind
     */
    public static void bindVertexArray(int vao) {
        BufferUploader.invalidate();
        GlStateManager._glBindVertexArray(vao);
    }

    /**
     * @return The veil renderer instance
     */
    public static VeilRenderer renderer() {
        return renderer;
    }

    /**
     * @return An executor for the main render thread
     */
    public static Executor renderThreadExecutor() {
        return RENDER_THREAD_EXECUTOR;
    }

    /**
     * @return The actual shader reference to use while rendering or <code>null</code> if no shader is selected or the selected shader is from Vanilla Minecraft
     */
    public static @Nullable ShaderProgram getShader() {
        ShaderInstance shader = RenderSystem.getShader();
        return shader instanceof ShaderProgramImpl.Wrapper wrapper ? wrapper.program() : null;
    }

    // Internal

    @ApiStatus.Internal
    public static void beginFrame() {
        VeilImGuiImpl.get().begin();
    }

    @ApiStatus.Internal
    public static void endFrame() {
        VeilImGuiImpl.get().end();
        renderer.getFramebufferManager().clear();
    }

    @ApiStatus.Internal
    public static void shaderUpdate() {
        VeilRenderSystem.shaderLocation = null;
    }

    @ApiStatus.Internal
    public static void resize(int width, int height) {
        if (renderer != null) {
            renderer.getFramebufferManager().resizeFramebuffers(width, height);
        }
    }

    @ApiStatus.Internal
    public static void close() {
        VeilImGuiImpl.get().free();
        VeilOpenCL.tryFree();
        if (renderer != null) {
            renderer.free();
        }
    }

    @ApiStatus.Internal
    public static void renderPost() {
        renderer.getPostProcessingManager().runPipeline();
    }
}
