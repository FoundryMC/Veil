package foundry.veil.api.client.render.shader.program;

import org.joml.*;
import org.lwjgl.opengl.GL31C;

import static org.lwjgl.opengl.GL31C.GL_INVALID_INDEX;

/**
 * Provides read and write access to all uniform variables in a shader program.
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
    int getUniform(CharSequence name);

    /**
     * Checks if the specified uniform exists in the shader.
     *
     * @param name The name of the uniform to check
     * @return Whether that uniform can be set
     */
    default boolean hasUniform(CharSequence name) {
        return this.getUniform(name) != -1;
    }

    /**
     * Retrieves the location of a uniform block.
     *
     * @param name The name of the uniform block to get
     * @return The location of that uniform block or {@value GL31C#GL_INVALID_INDEX} if not found
     */
    int getUniformBlock(CharSequence name);

    /**
     * Checks if the specified uniform block exists in the shader.
     *
     * @param name The name of the uniform block to check
     * @return Whether that uniform block can be set
     */
    default boolean hasUniformBlock(CharSequence name) {
        return this.getUniformBlock(name) != GL_INVALID_INDEX;
    }

    /**
     * Retrieves the location of a storage block.
     *
     * @param name The name of the storage block to get
     * @return The location of that storage block or {@value GL31C#GL_INVALID_INDEX} if not found
     */
    int getStorageBlock(CharSequence name);

    /**
     * Checks if the specified storage block exists in the shader.
     *
     * @param name The name of the storage block to check
     * @return Whether that storage block can be set
     */
    default boolean hasStorageBlock(CharSequence name) {
        return this.getStorageBlock(name) != GL_INVALID_INDEX;
    }

    /**
     * Retrieves a single float by the specified name.
     *
     * @param name The name of the uniform to get
     * @return The float value of that uniform
     */
    float getFloat(CharSequence name);

    /**
     * Retrieves a single integer by the specified name.
     *
     * @param name The name of the uniform to get
     * @return The int value of that uniform
     */
    int getInt(CharSequence name);

    /**
     * Retrieves an array of floats by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    void getFloats(CharSequence name, float[] values);

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    void getVector(CharSequence name, Vector2f... values);

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    void getVector(CharSequence name, Vector3f... values);

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    void getVector(CharSequence name, Vector4f... values);

    /**
     * Retrieves an array of integers by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    void getInts(CharSequence name, int[] values);

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    void getVector(CharSequence name, Vector2i... values);

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    void getVector(CharSequence name, Vector3i... values);

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    void getVector(CharSequence name, Vector4i... values);

    /**
     * Retrieves a matrix2x2 by the specified name
     *
     * @param name  The name of the uniform to get
     * @param value The value to set
     */
    void getMatrix(CharSequence name, Matrix2f value);

    /**
     * Retrieves a matrix3x3 by the specified name
     *
     * @param name  The name of the uniform to get
     * @param value The value to set
     */
    void getMatrix(CharSequence name, Matrix3f value);

    /**
     * Retrieves a matrix3x2 by the specified name
     *
     * @param name  The name of the uniform to get
     * @param value The value to set
     */
    void getMatrix(CharSequence name, Matrix3x2f value);

    /**
     * Retrieves a matrix4x4 by the specified name
     *
     * @param name  The name of the uniform to get
     * @param value The value to set
     */
    void getMatrix(CharSequence name, Matrix4f value);

    /**
     * Retrieves a matrix4x3 by the specified name
     *
     * @param name  The name of the uniform to get
     * @param value The value to set
     */
    void getMatrix(CharSequence name, Matrix4x3f value);
}
