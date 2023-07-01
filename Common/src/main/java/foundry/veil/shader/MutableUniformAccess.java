package foundry.veil.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.framebuffer.AdvancedFbo;
import foundry.veil.framebuffer.AdvancedFboTextureAttachment;
import foundry.veil.shader.texture.ShaderTextureSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL31C.GL_INVALID_INDEX;
import static org.lwjgl.opengl.GL31C.glUniformBlockBinding;
import static org.lwjgl.opengl.GL41C.*;

/**
 * Provides read and write access to all uniform variables in a shader program.
 *
 * @author Ocelot
 */
public interface MutableUniformAccess extends UniformAccess {

    /**
     * Sets default uniforms based on what {@link RenderSystem} provides.
     */
    default void applyRenderSystem() {
        this.setMatrix("RenderModelViewMat", RenderSystem.getModelViewMatrix());
        this.setMatrix("RenderProjMat", RenderSystem.getProjectionMatrix());
        float[] color = RenderSystem.getShaderColor();
        this.setVector("ColorModulator", color[0], color[1], color[2], color[3]);
        this.setFloat("GameTime", RenderSystem.getShaderGameTime());
    }

    /**
     * Sets <code>DiffuseSampler0</code>-<code>DiffuseSamplerMax</code>
     * to the color buffers in the specified framebuffer.
     *
     * @param framebuffer The framebuffer to bind samplers from
     */
    default void setFramebufferSamplers(@NotNull AdvancedFbo framebuffer) {
        Objects.requireNonNull(framebuffer, "framebuffer");

        int activeTexture = GlStateManager._getActiveTexture();
        for (int i = 0; i < framebuffer.getColorAttachments(); i++) {
            if (!framebuffer.isColorTextureAttachment(i)) {
                continue;
            }

            AdvancedFboTextureAttachment attachment = framebuffer.getColorTextureAttachment(i);
            this.addSampler("DiffuseSampler" + i, attachment.getId());
            if (attachment.getName() != null) {
                this.addSampler(attachment.getName(), attachment.getId());
            }
        }

        if (framebuffer.isDepthTextureAttachment()) {
            AdvancedFboTextureAttachment attachment = framebuffer.getDepthTextureAttachment();
            this.addSampler("DiffuseDepthSampler", framebuffer.getDepthTextureAttachment().getId());
            if (attachment.getName() != null) {
                this.addSampler(attachment.getName(), attachment.getId());
            }
        }

        RenderSystem.activeTexture(activeTexture);
    }

    /**
     * Loads the samplers set by {@link #addSampler(CharSequence, int)} into the shader.
     *
     * @param sampler The sampler to start binding to
     * @return The next available sampler
     */
    default int applyShaderSamplers(int sampler) {
        return this.applyShaderSamplers(ShaderTextureSource.GLOBAL_CONTEXT, sampler);
    }

    /**
     * Loads the samplers set by {@link #addSampler(CharSequence, int)} into the shader.
     *
     * @param context The context for setting built-in shader samplers or <code>null</code> to ignore normal samplers
     * @param sampler The sampler to start binding to
     * @return The next available sampler
     */
    int applyShaderSamplers(@Nullable ShaderTextureSource.Context context, int sampler);

    /**
     * Adds a texture that is dynamically bound and sets texture units.
     *
     * @param name      The name of the texture to set
     * @param textureId The id of the texture to bind and assign a texture unit
     */
    void addSampler(@NotNull CharSequence name, int textureId);

    /**
     * Removes the specified sampler binding.
     *
     * @param name The name of the sampler to remove
     */
    void removeSampler(@NotNull CharSequence name);

    /**
     * Clears all samplers.
     */
    void clearSamplers();

    /**
     * Sets the binding to use for the specified uniform block.
     *
     * @param name    The name of the block to set
     * @param binding The binding to use for that block
     */
    default void setUniformBlock(@NotNull CharSequence name, int binding) {
        int index = this.getUniformBlock(name);
        if (index != GL_INVALID_INDEX) {
            glUniformBlockBinding(this.getProgram(), index, binding);
        }
    }

    /**
     * Sets a float in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setFloat(@NotNull CharSequence name, float value) {
        int location = this.getUniform(name);
        if (location != -1) {
            glProgramUniform1f(this.getProgram(), location, value);
        }
    }

    /**
     * Sets a vector in the shader.
     *
     * @param name The name of the uniform to set
     * @param x    The x component of the vector
     * @param y    The y component of the vector
     */
    default void setVector(@NotNull CharSequence name, float x, float y) {
        int location = this.getUniform(name);
        if (location != -1) {
            glProgramUniform2f(this.getProgram(), location, x, y);
        }
    }

    /**
     * Sets a vector in the shader.
     *
     * @param name The name of the uniform to set
     * @param x    The x component of the vector
     * @param y    The y component of the vector
     * @param z    The z component of the vector
     */
    default void setVector(@NotNull CharSequence name, float x, float y, float z) {
        int location = this.getUniform(name);
        if (location != -1) {
            glProgramUniform3f(this.getProgram(), location, x, y, z);
        }
    }

    /**
     * Sets a vector in the shader.
     *
     * @param name The name of the uniform to set
     * @param x    The x component of the vector
     * @param y    The y component of the vector
     * @param z    The z component of the vector
     * @param w    The w component of the vector
     */
    default void setVector(@NotNull CharSequence name, float x, float y, float z, float w) {
        int location = this.getUniform(name);
        if (location != -1) {
            glProgramUniform4f(this.getProgram(), location, x, y, z, w);
        }
    }

    /**
     * Sets a vector in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setVector(@NotNull CharSequence name, @NotNull Vector2fc value) {
        Objects.requireNonNull(value, "value");
        this.setVector(name, value.x(), value.y());
    }

    /**
     * Sets a vector in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setVector(@NotNull CharSequence name, @NotNull Vector3fc value) {
        Objects.requireNonNull(value, "value");
        this.setVector(name, value.x(), value.y(), value.z());
    }

    /**
     * Sets a vector in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setVector(@NotNull CharSequence name, @NotNull Vector4fc value) {
        Objects.requireNonNull(value, "value");
        this.setVector(name, value.x(), value.y(), value.z(), value.w());
    }

    /**
     * Sets an integer in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setInt(@NotNull CharSequence name, int value) {
        int location = this.getUniform(name);
        if (location != -1) {
            glProgramUniform1i(this.getProgram(), location, value);
        }
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param name The name of the uniform to set
     * @param x    The x component of the vector
     * @param y    The y component of the vector
     */
    default void setVectorI(@NotNull CharSequence name, int x, int y) {
        int location = this.getUniform(name);
        if (location != -1) {
            glProgramUniform2i(this.getProgram(), location, x, y);
        }
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param name The name of the uniform to set
     * @param x    The x component of the vector
     * @param y    The y component of the vector
     * @param z    The z component of the vector
     */
    default void setVectorI(@NotNull CharSequence name, int x, int y, int z) {
        int location = this.getUniform(name);
        if (location != -1) {
            glProgramUniform3i(this.getProgram(), location, x, y, z);
        }
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param name The name of the uniform to set
     * @param x    The x component of the vector
     * @param y    The y component of the vector
     * @param z    The z component of the vector
     * @param w    The w component of the vector
     */
    default void setVectorI(@NotNull CharSequence name, int x, int y, int z, int w) {
        int location = this.getUniform(name);
        if (location != -1) {
            glProgramUniform4i(this.getProgram(), location, x, y, z, w);
        }
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setVectorI(@NotNull CharSequence name, @NotNull Vector2ic value) {
        Objects.requireNonNull(value, "value");
        this.setVectorI(name, value.x(), value.y());
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setVectorI(@NotNull CharSequence name, @NotNull Vector3ic value) {
        Objects.requireNonNull(value, "value");
        this.setVectorI(name, value.x(), value.y(), value.z());
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setVectorI(@NotNull CharSequence name, @NotNull Vector4ic value) {
        Objects.requireNonNull(value, "value");
        this.setVectorI(name, value.x(), value.y(), value.z(), value.w());
    }

    /**
     * Sets an array of floats in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setFloats(@NotNull CharSequence name, float @NotNull ... values) {
        int location = this.getUniform(name);
        if (location != -1) {
            glProgramUniform1fv(this.getProgram(), location, values);
        }
    }

    /**
     * Sets an array of vectors in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setVectors(@NotNull CharSequence name, Vector2fc @NotNull ... values) {
        Objects.requireNonNull(values, "values");
        int location = this.getUniform(name);
        if (location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(values.length * 2);
            for (int i = 0; i < values.length; i++) {
                values[i].get(i * 2, buffer);
            }
            glProgramUniform2fv(this.getProgram(), location, buffer);
        }
    }

    /**
     * Sets an array of vectors in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setVectors(@NotNull CharSequence name, Vector3fc @NotNull ... values) {
        Objects.requireNonNull(values, "values");
        int location = this.getUniform(name);
        if (location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(values.length * 3);
            for (int i = 0; i < values.length; i++) {
                values[i].get(i * 3, buffer);
            }
            glProgramUniform3fv(this.getProgram(), location, buffer);
        }
    }

    /**
     * Sets an array of vectors in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setVectors(@NotNull CharSequence name, Vector4fc @NotNull ... values) {
        Objects.requireNonNull(values, "values");
        int location = this.getUniform(name);
        if (location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(values.length * 4);
            for (int i = 0; i < values.length; i++) {
                values[i].get(i * 4, buffer);
            }
            glProgramUniform4fv(this.getProgram(), location, buffer);
        }
    }

    /**
     * Sets an array of integers in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setInts(@NotNull CharSequence name, int @NotNull ... values) {
        int location = this.getUniform(name);
        if (location != -1) {
            glProgramUniform1iv(this.getProgram(), location, values);
        }
    }

    /**
     * Sets an array of integer vectors in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setVectors(@NotNull CharSequence name, Vector2ic @NotNull ... values) {
        Objects.requireNonNull(values, "values");
        int location = this.getUniform(name);
        if (location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(values.length * 2);
            for (int i = 0; i < values.length; i++) {
                values[i].get(i * 2, buffer);
            }
            glProgramUniform2iv(this.getProgram(), location, buffer);
        }
    }

    /**
     * Sets an array of integer vectors in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setVectors(@NotNull CharSequence name, Vector3ic @NotNull ... values) {
        Objects.requireNonNull(values, "values");
        int location = this.getUniform(name);
        if (location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(values.length * 3);
            for (int i = 0; i < values.length; i++) {
                values[i].get(i * 3, buffer);
            }
            glProgramUniform3iv(this.getProgram(), location, buffer);
        }
    }

    /**
     * Sets an array of integer vectors in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setVectors(@NotNull CharSequence name, Vector4ic @NotNull ... values) {
        Objects.requireNonNull(values, "values");
        int location = this.getUniform(name);
        if (location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(values.length * 4);
            for (int i = 0; i < values.length; i++) {
                values[i].get(i * 4, buffer);
            }
            glProgramUniform4iv(this.getProgram(), location, buffer);
        }
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setMatrix(@NotNull CharSequence name, @NotNull Matrix2fc value) {
        Objects.requireNonNull(value, "value");
        int location = this.getUniform(name);
        if (location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(2 * 2);
            value.get(buffer);
            glProgramUniformMatrix2fv(this.getProgram(), location, false, buffer);
        }
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setMatrix(@NotNull CharSequence name, @NotNull Matrix3fc value) {
        Objects.requireNonNull(value, "value");
        int location = this.getUniform(name);
        if (location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3 * 3);
            value.get(buffer);
            glProgramUniformMatrix3fv(this.getProgram(), location, false, buffer);
        }
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setMatrix(@NotNull CharSequence name, @NotNull Matrix3x2fc value) {
        Objects.requireNonNull(value, "value");
        int location = this.getUniform(name);
        if (location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3 * 2);
            value.get(buffer);
            glProgramUniformMatrix3x2fv(this.getProgram(), location, false, buffer);
        }
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setMatrix(@NotNull CharSequence name, @NotNull Matrix4fc value) {
        Objects.requireNonNull(value, "value");
        int location = this.getUniform(name);
        if (location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4 * 4);
            value.get(buffer);
            glProgramUniformMatrix4fv(this.getProgram(), location, false, buffer);
        }
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setMatrix(@NotNull CharSequence name, @NotNull Matrix4x3fc value) {
        Objects.requireNonNull(value, "value");
        int location = this.getUniform(name);
        if (location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4 * 3);
            value.get(buffer);
            glProgramUniformMatrix4x3fv(this.getProgram(), location, false, buffer);
        }
    }
}
