package foundry.veil.shader;

import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL20C.*;

/**
 * Provides read access to all uniform variables in a shader program.
 *
 * @author Ocelot
 */
public interface UniformAccess {

    /**
     * Retrieves the location of a uniform.
     *
     * @param name The name of the uniform to get
     * @return The location of that uniform or <code>-1</code> if not found
     */
    int getUniform(@NotNull CharSequence name);

    /**
     * Retrieves the location of a uniform block.
     *
     * @param name The name of the uniform block to get
     * @return The location of that uniform block or <code>-1</code> if not found
     */
    int getUniformBlock(@NotNull CharSequence name);

    /**
     * @return The OpenGL id of this program
     */
    int getProgram();

    /**
     * Retrieves a single float by the specified name.
     *
     * @param name The name of the uniform to get
     * @return The float value of that uniform
     */
    default float getFloat(@NotNull CharSequence name) {
        return glGetUniformf(this.getProgram(), this.getUniform(name));
    }

    /**
     * Retrieves a single integer by the specified name.
     *
     * @param name The name of the uniform to get
     * @return The int value of that uniform
     */
    default int getInt(@NotNull CharSequence name) {
        return glGetUniformi(this.getProgram(), this.getUniform(name));
    }

    /**
     * Retrieves an array of floats by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    default void getFloats(@NotNull CharSequence name, float @NotNull [] values) {
        Objects.requireNonNull(values, "values");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.callocFloat(values.length);
            glGetUniformfv(this.getProgram(), this.getUniform(name), buffer);
            for (int i = 0; i < values.length; i++) {
                values[i] = buffer.get(i);
            }
        }
    }

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    default void getVector(@NotNull CharSequence name, Vector2f @NotNull ... values) {
        Objects.requireNonNull(values, "values");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.callocFloat(values.length * 2);
            glGetUniformfv(this.getProgram(), this.getUniform(name), buffer);
            for (int i = 0; i < values.length; i++) {
                values[i].set(i * 2, buffer);
            }
        }
    }

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    default void getVector(@NotNull CharSequence name, Vector3f @NotNull ... values) {
        Objects.requireNonNull(values, "values");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.callocFloat(values.length * 3);
            glGetUniformfv(this.getProgram(), this.getUniform(name), buffer);
            for (int i = 0; i < values.length; i++) {
                values[i].set(i * 3, buffer);
            }
        }
    }

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    default void getVector(@NotNull CharSequence name, Vector4f @NotNull ... values) {
        Objects.requireNonNull(values, "values");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.callocFloat(values.length * 4);
            glGetUniformfv(this.getProgram(), this.getUniform(name), buffer);
            for (int i = 0; i < values.length; i++) {
                values[i].set(i * 4, buffer);
            }
        }
    }

    /**
     * Retrieves an array of integers by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    default void getInts(@NotNull CharSequence name, int @NotNull [] values) {
        Objects.requireNonNull(values, "values");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.callocInt(values.length);
            glGetUniformiv(this.getProgram(), this.getUniform(name), buffer);
            for (int i = 0; i < values.length; i++) {
                values[i] = buffer.get(i);
            }
        }
    }

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    default void getVector(@NotNull CharSequence name, Vector2i @NotNull ... values) {
        Objects.requireNonNull(values, "values");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.callocInt(values.length * 2);
            glGetUniformiv(this.getProgram(), this.getUniform(name), buffer);
            for (int i = 0; i < values.length; i++) {
                values[i].set(i * 2, buffer);
            }
        }
    }

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    default void getVector(@NotNull CharSequence name, Vector3i @NotNull ... values) {
        Objects.requireNonNull(values, "values");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.callocInt(values.length * 3);
            glGetUniformiv(this.getProgram(), this.getUniform(name), buffer);
            for (int i = 0; i < values.length; i++) {
                values[i].set(i * 3, buffer);
            }
        }
    }

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    default void getVector(@NotNull CharSequence name, Vector4i @NotNull ... values) {
        Objects.requireNonNull(values, "values");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.callocInt(values.length * 4);
            glGetUniformiv(this.getProgram(), this.getUniform(name), buffer);
            for (int i = 0; i < values.length; i++) {
                values[i].set(i * 4, buffer);
            }
        }
    }

    /**
     * Retrieves a matrix2x2 by the specified name
     *
     * @param name  The name of the uniform to get
     * @param value The value to set
     */
    default void getMatrix(@NotNull CharSequence name, @NotNull Matrix2f value) {
        Objects.requireNonNull(value, "value");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.callocFloat(2 * 2);
            glGetUniformfv(this.getProgram(), this.getUniform(name), buffer);
            value.set(0, buffer);
        }
    }

    /**
     * Retrieves a matrix3x3 by the specified name
     *
     * @param name  The name of the uniform to get
     * @param value The value to set
     */
    default void getMatrix(@NotNull CharSequence name, @NotNull Matrix3f value) {
        Objects.requireNonNull(value, "value");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.callocFloat(3 * 3);
            glGetUniformfv(this.getProgram(), this.getUniform(name), buffer);
            value.set(0, buffer);
        }
    }

    /**
     * Retrieves a matrix3x2 by the specified name
     *
     * @param name  The name of the uniform to get
     * @param value The value to set
     */
    default void getMatrix(@NotNull CharSequence name, @NotNull Matrix3x2f value) {
        Objects.requireNonNull(value, "value");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.callocFloat(3 * 2);
            glGetUniformfv(this.getProgram(), this.getUniform(name), buffer);
            value.set(0, buffer);
        }
    }

    /**
     * Retrieves a matrix4x4 by the specified name
     *
     * @param name  The name of the uniform to get
     * @param value The value to set
     */
    default void getMatrix(@NotNull CharSequence name, @NotNull Matrix4f value) {
        Objects.requireNonNull(value, "value");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.callocFloat(4 * 4);
            glGetUniformfv(this.getProgram(), this.getUniform(name), buffer);
            value.set(0, buffer);
        }
    }

    /**
     * Retrieves a matrix4x3 by the specified name
     *
     * @param name  The name of the uniform to get
     * @param value The value to set
     */
    default void getMatrix(@NotNull CharSequence name, @NotNull Matrix4x3f value) {
        Objects.requireNonNull(value, "value");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.callocFloat(4 * 3);
            glGetUniformfv(this.getProgram(), this.getUniform(name), buffer);
            value.set(0, buffer);
        }
    }
}
