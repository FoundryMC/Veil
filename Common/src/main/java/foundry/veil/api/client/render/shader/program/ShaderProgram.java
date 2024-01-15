package foundry.veil.api.client.render.shader.program;

import com.mojang.blaze3d.shaders.Uniform;
import foundry.veil.api.client.render.shader.CompiledShader;
import foundry.veil.api.client.render.shader.ShaderCompiler;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.impl.client.render.shader.ShaderProgramImpl;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.NativeResource;

import java.util.Set;

import static org.lwjgl.opengl.GL20C.glUseProgram;

/**
 * Represents a usable shader program with shaders attached.
 *
 * @author Ocelot
 */
public interface ShaderProgram extends NativeResource, MutableShaderUniformAccess {

    /**
     * Binds this program for use and prepares for rendering.
     */
    default void setup() {
        this.bind();
        this.addRenderSystemTextures();
        this.applyShaderSamplers(0);
    }

    /**
     * Binds this program for use.
     */
    default void bind() {
        glUseProgram(this.getProgram());
    }

    /**
     * Unbinds the currently bound shader program.
     */
    static void unbind() {
        glUseProgram(0);
    }

    /**
     * Compiles this shader based on the specified definition.
     *
     * @param context  The context to use when compiling shaders
     * @param compiler The compiler to use
     * @throws Exception If an error occurs while compiling or linking shaders
     */
    void compile(ShaderCompiler.Context context, ShaderCompiler compiler) throws Exception;

    /**
     * @return The shaders attached to this program
     */
    Set<CompiledShader> getShaders();

    /**
     * @return All shader definitions this program depends on
     */
    Set<String> getDefinitionDependencies();

    /**
     * @return The id of this program
     */
    ResourceLocation getId();

    /**
     * <p>Wraps this shader with a vanilla Minecraft shader instance wrapper. There are a few special properties about the shader wrapper.</p>
     * <ul>
     *     <li>The shader instance cannot be used to free the shader program. {@link ShaderProgram#free()} must be called separately.
     *     If the shader is loaded through {@link ShaderManager} then there is no need to free the shader.</li>
     *     <li>Calling {@link Uniform#upload()} will do nothing since the values are uploaded when the appropriate methods are called</li>
     *     <li>Uniforms are lazily wrapped and will not crash when the wrong method is called.</li>
     *     <li>{@link Uniform#set(int, float)} is not supported and will throw an {@link UnsupportedOperationException}.</li>
     *     <li>Only {@link Uniform#set(Matrix3f)} and {@link Uniform#set(Matrix4f)} will be able to set matrix values. All other matrix methods will throw an {@link UnsupportedOperationException}.</li>
     *     <li>{@link Uniform#set(float[])} only works for 1, 2, 3, and 4 float elements. Any other size will throw an {@link UnsupportedOperationException}.</li>
     * </ul>
     *
     * @return A lazily loaded shader instance wrapper for this program
     */
    ShaderInstance toShaderInstance();

    /**
     * Creates a new shader program with the specified id.
     *
     * @param id The id of the program
     * @return A new shader program
     */
    static ShaderProgram create(ResourceLocation id) {
        return new ShaderProgramImpl(id);
    }
}
