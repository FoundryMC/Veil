package foundry.veil.render.shader.program;

import org.joml.*;

/**
 * Provides write access to all uniform variables in a shader program.
 *
 * @author Ocelot
 */
public interface UniformAccess {

    /**
     * Sets the binding to use for the specified uniform block.
     *
     * @param name    The name of the block to set
     * @param binding The binding to use for that block
     */
    default void setUniformBlock(CharSequence name, int binding) {
    }

    /**
     * Sets a float in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setFloat(CharSequence name, float value) {
    }

    /**
     * Sets a vector in the shader.
     *
     * @param name The name of the uniform to set
     * @param x    The x component of the vector
     * @param y    The y component of the vector
     */
    default void setVector(CharSequence name, float x, float y) {
    }

    /**
     * Sets a vector in the shader.
     *
     * @param name The name of the uniform to set
     * @param x    The x component of the vector
     * @param y    The y component of the vector
     * @param z    The z component of the vector
     */
    default void setVector(CharSequence name, float x, float y, float z) {
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
    default void setVector(CharSequence name, float x, float y, float z, float w) {
    }

    /**
     * Sets a vector in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setVector(CharSequence name, Vector2fc value) {
        this.setVector(name, value.x(), value.y());
    }

    /**
     * Sets a vector in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setVector(CharSequence name, Vector3fc value) {
        this.setVector(name, value.x(), value.y(), value.z());
    }

    /**
     * Sets a vector in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setVector(CharSequence name, Vector4fc value) {
        this.setVector(name, value.x(), value.y(), value.z(), value.w());
    }

    /**
     * Sets an integer in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setInt(CharSequence name, int value) {
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param name The name of the uniform to set
     * @param x    The x component of the vector
     * @param y    The y component of the vector
     */
    default void setVectorI(CharSequence name, int x, int y) {
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param name The name of the uniform to set
     * @param x    The x component of the vector
     * @param y    The y component of the vector
     * @param z    The z component of the vector
     */
    default void setVectorI(CharSequence name, int x, int y, int z) {
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
    default void setVectorI(CharSequence name, int x, int y, int z, int w) {
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setVectorI(CharSequence name, Vector2ic value) {
        this.setVectorI(name, value.x(), value.y());
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setVectorI(CharSequence name, Vector3ic value) {
        this.setVectorI(name, value.x(), value.y(), value.z());
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setVectorI(CharSequence name, Vector4ic value) {
        this.setVectorI(name, value.x(), value.y(), value.z(), value.w());
    }

    /**
     * Sets an array of floats in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setFloats(CharSequence name, float... values) {
    }

    /**
     * Sets an array of vectors in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setVectors(CharSequence name, Vector2fc... values) {
    }

    /**
     * Sets an array of vectors in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setVectors(CharSequence name, Vector3fc... values) {
    }

    /**
     * Sets an array of vectors in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setVectors(CharSequence name, Vector4fc... values) {
    }

    /**
     * Sets an array of integers in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setInts(CharSequence name, int... values) {
    }

    /**
     * Sets an array of integer vectors in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setVectors(CharSequence name, Vector2ic... values) {
    }

    /**
     * Sets an array of integer vectors in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setVectors(CharSequence name, Vector3ic... values) {
    }

    /**
     * Sets an array of integer vectors in the shader.
     *
     * @param name   The name of the uniform to set
     * @param values The values to set in order
     */
    default void setVectors(CharSequence name, Vector4ic... values) {
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setMatrix(CharSequence name, Matrix2fc value) {
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setMatrix(CharSequence name, Matrix3fc value) {
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setMatrix(CharSequence name, Matrix3x2fc value) {
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setMatrix(CharSequence name, Matrix4fc value) {
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param name  The name of the uniform to set
     * @param value The value to set
     */
    default void setMatrix(CharSequence name, Matrix4x3fc value) {
    }
}
