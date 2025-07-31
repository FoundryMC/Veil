package foundry.veil.api.client.render.shader.uniform;

import foundry.veil.impl.client.render.shader.uniform.CompositeShaderUniformAccess;
import foundry.veil.impl.client.render.shader.uniform.EmptyShaderUniformAccess;
import org.joml.*;

import java.lang.Math;

/**
 * Provides access to 1 or more shader uniforms.
 */
public interface ShaderUniformAccess {

    /**
     * NO-OP Uniform Access
     */
    ShaderUniformAccess EMPTY = EmptyShaderUniformAccess.INSTANCE;

    /**
     * Creates a composite shader uniform access.
     *
     * @param accesses The uniforms to merge together
     * @return A new {@link ShaderUniformAccess} that sets values in the provided access
     */
    static ShaderUniformAccess of(ShaderUniformAccess... accesses) {
        if (accesses.length == 0) {
            return ShaderUniformAccess.EMPTY;
        }
        if (accesses.length == 1) {
            return accesses[0];
        }
        return new CompositeShaderUniformAccess(accesses);
    }

    /**
     * @return Whether this uniform is valid
     */
    boolean isValid();

    /**
     * Sets a float in the shader.
     *
     * @param value The value to set
     */
    void setFloat(float value);

    /**
     * Sets a vector in the shader.
     *
     * @param x The x component of the vector
     * @param y The y component of the vector
     */
    void setVector(float x, float y);

    /**
     * Sets a vector in the shader.
     *
     * @param x The x component of the vector
     * @param y The y component of the vector
     * @param z The z component of the vector
     */
    void setVector(float x, float y, float z);

    /**
     * Sets a vector in the shader.
     *
     * @param x The x component of the vector
     * @param y The y component of the vector
     * @param z The z component of the vector
     * @param w The w component of the vector
     */
    void setVector(float x, float y, float z, float w);

    /**
     * Sets a vector in the shader.
     *
     * @param value The value to set
     */
    default void setVector(Vector2fc value) {
        this.setVector(value.x(), value.y());
    }

    /**
     * Sets a vector in the shader.
     *
     * @param value The value to set
     */
    default void setVector(Vector3fc value) {
        this.setVector(value.x(), value.y(), value.z());
    }

    /**
     * Sets a vector in the shader.
     *
     * @param value The value to set
     */
    default void setVector(Vector4fc value) {
        this.setVector(value.x(), value.y(), value.z(), value.w());
    }

    /**
     * Sets a vector in the shader.
     *
     * @param values The values to set
     * @throws UnsupportedOperationException If the array passed in is empty
     */
    default void setVector(float[] values) {
        switch (Math.min(4, values.length)) {
            case 1 -> this.setFloat(values[0]);
            case 2 -> this.setVector(values[0], values[1]);
            case 3 -> this.setVector(values[0], values[1], values[2]);
            case 4 -> this.setVector(values[0], values[1], values[2], values[3]);
            default -> throw new UnsupportedOperationException("At least 1 value must be specified");
        }
    }

    /**
     * Sets an integer in the shader.
     *
     * @param value The value to set
     */
    void setInt(int value);

    /**
     * Sets an integer vector in the shader.
     *
     * @param x The x component of the vector
     * @param y The y component of the vector
     */
    void setVectorI(int x, int y);

    /**
     * Sets an integer vector in the shader.
     *
     * @param x The x component of the vector
     * @param y The y component of the vector
     * @param z The z component of the vector
     */
    void setVectorI(int x, int y, int z);

    /**
     * Sets an integer vector in the shader.
     *
     * @param x The x component of the vector
     * @param y The y component of the vector
     * @param z The z component of the vector
     * @param w The w component of the vector
     */
    void setVectorI(int x, int y, int z, int w);

    /**
     * Sets an integer vector in the shader.
     *
     * @param value The value to set
     */
    default void setVectorI(Vector2ic value) {
        this.setVectorI(value.x(), value.y());
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param value The value to set
     */
    default void setVectorI(Vector3ic value) {
        this.setVectorI(value.x(), value.y(), value.z());
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param value The value to set
     */
    default void setVectorI(Vector4ic value) {
        this.setVectorI(value.x(), value.y(), value.z(), value.w());
    }

    /**
     * Sets an integer vector in the shader.
     *
     * @param values The values to set
     * @throws UnsupportedOperationException If the array passed in is empty
     */
    default void setVectorI(int[] values) {
        switch (Math.min(4, values.length)) {
            case 1 -> this.setInt(values[0]);
            case 2 -> this.setVectorI(values[0], values[1]);
            case 3 -> this.setVectorI(values[0], values[1], values[2]);
            case 4 -> this.setVectorI(values[0], values[1], values[2], values[3]);
            default -> throw new UnsupportedOperationException("At least 1 value must be specified");
        }
    }

    /**
     * Sets a double in the shader.
     *
     * @param value The value to set
     */
    void setDouble(double value);

    /**
     * Sets a double vector in the shader.
     *
     * @param x The x component of the vector
     * @param y The y component of the vector
     */
    void setVector64(double x, double y);

    /**
     * Sets a double vector in the shader.
     *
     * @param x The x component of the vector
     * @param y The y component of the vector
     * @param z The z component of the vector
     */
    void setVector64(double x, double y, double z);

    /**
     * Sets a double vector in the shader.
     *
     * @param x The x component of the vector
     * @param y The y component of the vector
     * @param z The z component of the vector
     * @param w The w component of the vector
     */
    void setVector64(double x, double y, double z, double w);

    /**
     * Sets a double vector in the shader.
     *
     * @param value The value to set
     */
    default void setVector64(Vector2dc value) {
        this.setVector64(value.x(), value.y());
    }

    /**
     * Sets a double vector in the shader.
     *
     * @param value The value to set
     */
    default void setVector64(Vector3dc value) {
        this.setVector64(value.x(), value.y(), value.z());
    }

    /**
     * Sets a double vector in the shader.
     *
     * @param value The value to set
     */
    default void setVector64(Vector4dc value) {
        this.setVector64(value.x(), value.y(), value.z(), value.w());
    }

    /**
     * Sets a double vector in the shader.
     *
     * @param values The values to set
     * @throws UnsupportedOperationException If the array passed in is empty
     */
    default void setVector64(double[] values) {
        switch (Math.min(4, values.length)) {
            case 1 -> this.setDouble(values[0]);
            case 2 -> this.setVector64(values[0], values[1]);
            case 3 -> this.setVector64(values[0], values[1], values[2]);
            case 4 -> this.setVector64(values[0], values[1], values[2], values[3]);
            default -> throw new UnsupportedOperationException("At least 1 value must be specified");
        }
    }

    /**
     * Sets a long in the shader.
     *
     * @param value The value to set
     */
    void setLong(long value);

    /**
     * Sets a long vector in the shader.
     *
     * @param x The x component of the vector
     * @param y The y component of the vector
     */
    void setVectorI64(long x, long y);

    /**
     * Sets a long vector in the shader.
     *
     * @param x The x component of the vector
     * @param y The y component of the vector
     * @param z The z component of the vector
     */
    void setVectorI64(long x, long y, long z);

    /**
     * Sets a long vector in the shader.
     *
     * @param x The x component of the vector
     * @param y The y component of the vector
     * @param z The z component of the vector
     * @param w The w component of the vector
     */
    void setVectorI64(long x, long y, long z, long w);

    /**
     * Sets a long vector in the shader.
     *
     * @param values The values to set
     * @throws UnsupportedOperationException If the array passed in is empty
     */
    default void setVectorI64(long[] values) {
        switch (Math.min(4, values.length)) {
            case 1 -> this.setLong(values[0]);
            case 2 -> this.setVectorI64(values[0], values[1]);
            case 3 -> this.setVectorI64(values[0], values[1], values[2]);
            case 4 -> this.setVectorI64(values[0], values[1], values[2], values[3]);
            default -> throw new UnsupportedOperationException("At least 1 value must be specified");
        }
    }

    /**
     * Sets an array of floats in the shader.
     *
     * @param values The values to set in order
     */
    void setFloats(float... values);

    /**
     * Sets an array of vectors in the shader.
     *
     * @param values The values to set in order
     */
    void setVectors(Vector2fc... values);

    /**
     * Sets an array of vectors in the shader.
     *
     * @param values The values to set in order
     */
    void setVectors(Vector3fc... values);

    /**
     * Sets an array of vectors in the shader.
     *
     * @param values The values to set in order
     */
    void setVectors(Vector4fc... values);

    /**
     * Sets an array of integers in the shader.
     *
     * @param values The values to set in order
     */
    void setInts(int... values);

    /**
     * Sets an array of integer vectors in the shader.
     *
     * @param values The values to set in order
     */
    void setIVectors(Vector2ic... values);

    /**
     * Sets an array of integer vectors in the shader.
     *
     * @param values The values to set in order
     */
    void setIVectors(Vector3ic... values);

    /**
     * Sets an array of integer vectors in the shader.
     *
     * @param values The values to set in order
     */
    void setIVectors(Vector4ic... values);

    /**
     * Sets an array of doubles in the shader.
     *
     * @param values The values to set in order
     */
    void setDoubles(double... values);

    /**
     * Sets an array of double vectors in the shader.
     *
     * @param values The values to set in order
     */
    void set64Vectors(Vector2dc... values);

    /**
     * Sets an array of double vectors in the shader.
     *
     * @param values The values to set in order
     */
    void set64Vectors(Vector3dc... values);

    /**
     * Sets an array of double vectors in the shader.
     *
     * @param values The values to set in order
     */
    void set64Vectors(Vector4dc... values);

    /**
     * Sets an array of longs in the shader.
     *
     * @param values The values to set in order
     */
    void setLongs(long... values);

    /**
     * Uploads this uniform as a handle type. Only supported if bindless texture is supported.
     * @param value The handle to upload
     * @since 2.0.0
     */
    void setHandle(long value);

    /**
     * Uploads this uniform as a handle type. Only supported if bindless texture is supported.
     * @param values The handles to upload
     * @since 2.0.0
     */
    void setHandles(long... values);

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix(Matrix2fc value) {
        this.setMatrix(value, false);
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix(Matrix3fc value) {
        this.setMatrix(value, false);
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix(Matrix4fc value) {
        this.setMatrix(value, false);
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix2x3(Matrix3x2fc value) {
        this.setMatrix2x3(value, false);
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix3x2(Matrix3x2fc value) {
        this.setMatrix3x2(value, false);
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix3x4(Matrix4x3fc value) {
        this.setMatrix3x4(value, false);
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix4x3(Matrix4x3fc value) {
        this.setMatrix4x3(value, false);
    }

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix(Matrix2fc value, boolean transpose);

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix(Matrix3fc value, boolean transpose);

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix(Matrix4fc value, boolean transpose);

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix2x3(Matrix3x2fc value, boolean transpose);

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix3x2(Matrix3x2fc value, boolean transpose);

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix3x4(Matrix4x3fc value, boolean transpose);

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix4x3(Matrix4x3fc value, boolean transpose);

    /**
     * Sets a matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix(Matrix2dc value) {
        this.setMatrix(value, false);
    }

    /**
     * Sets a double matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix(Matrix3dc value) {
        this.setMatrix(value, false);
    }

    /**
     * Sets a double matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix(Matrix4dc value) {
        this.setMatrix(value, false);
    }

    /**
     * Sets a double matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix2x3(Matrix3x2dc value) {
        this.setMatrix2x3(value, false);
    }

    /**
     * Sets a double matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix3x2(Matrix3x2dc value) {
        this.setMatrix3x2(value, false);
    }

    /**
     * Sets a double matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix3x4(Matrix4x3dc value) {
        this.setMatrix3x4(value, false);
    }

    /**
     * Sets a double matrix in the shader.
     *
     * @param value The value to set
     */
    default void setMatrix4x3(Matrix4x3dc value) {
        this.setMatrix4x3(value, false);
    }

    /**
     * Sets a double matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix(Matrix2dc value, boolean transpose);

    /**
     * Sets a double matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix(Matrix3dc value, boolean transpose);

    /**
     * Sets a double matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix(Matrix4dc value, boolean transpose);

    /**
     * Sets a double matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix2x3(Matrix3x2dc value, boolean transpose);

    /**
     * Sets a double matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix3x2(Matrix3x2dc value, boolean transpose);

    /**
     * Sets a double matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix3x4(Matrix4x3dc value, boolean transpose);

    /**
     * Sets a double matrix in the shader.
     *
     * @param value The value to set
     */
    void setMatrix4x3(Matrix4x3dc value, boolean transpose);
}
