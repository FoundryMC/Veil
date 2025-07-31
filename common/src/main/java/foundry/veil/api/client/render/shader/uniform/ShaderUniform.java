package foundry.veil.api.client.render.shader.uniform;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.program.ShaderUniformCache;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.*;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.ARBGPUShaderFP64.*;
import static org.lwjgl.opengl.ARBGPUShaderInt64.*;
import static org.lwjgl.opengl.ARBSeparateShaderObjects.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;

/**
 * A single uniform in a shader program.
 */
public interface ShaderUniform extends ShaderUniformAccess {

    /**
     * Invalidates the uniform cache.
     */
    void invalidateCache();

    @Override
    default boolean isValid() {
        return this.getLocation() != -1;
    }

    /**
     * @return The name assigned to this uniform in the shader
     */
    String getName();

    /**
     * @return The location of the uniform in the shader
     */
    int getLocation();

    /**
     * @return The data type of this uniform in the shader
     */
    Type getType();

    /**
     * @return The number of array elements in the uniform
     */
    int getArrayLength();

    /**
     * Retrieves the value of this uniform as a float.
     *
     * @return The float value of this uniform
     */
    float getFloat();

    /**
     * Retrieves the values of this uniform as a float array.
     *
     * @return A new array with the values of this uniform
     */
    default float[] getFloats() {
        float[] values = new float[this.getArrayLength()];
        this.getFloats(values, 0, values.length);
        return values;
    }

    /**
     * Retrieves the values of this uniform as a float array and stores them in the specified array.
     *
     * @param fill The array to fill
     */
    default void getFloats(float[] fill) {
        this.getFloats(fill, 0, fill.length);
    }

    /**
     * Retrieves the values of this uniform as a float array and stores them in the specified array.
     *
     * @param dst    The array to fill
     * @param offset The position to start filling the array at
     * @param length The number of values to read
     */
    void getFloats(float[] dst, int offset, int length);

    /**
     * Retrieves the value of this uniform as an integer.
     *
     * @return The int value of this uniform
     */
    int getInt();

    /**
     * Retrieves the values of this uniform as an int array.
     *
     * @return A new array with the values of this uniform
     */
    default int[] getInts() {
        int[] values = new int[this.getArrayLength()];
        this.getInts(values, 0, values.length);
        return values;
    }

    /**
     * Retrieves the values of this uniform as an int array and stores them in the specified array.
     *
     * @param fill The array to fill
     */
    default void getInts(int[] fill) {
        this.getInts(fill, 0, fill.length);
    }

    /**
     * Retrieves the values of this uniform as an int array and stores them in the specified array.
     *
     * @param dst    The array to fill
     * @param offset The position to start filling the array at
     * @param length The number of values to read
     */
    void getInts(int[] dst, int offset, int length);

    /**
     * Retrieves the value of this uniform as a double.
     *
     * @return The int value of this uniform
     */
    double getDouble();

    /**
     * Retrieves the values of this uniform as a double array.
     *
     * @return A new array with the values of this uniform
     */
    default double[] getDoubles() {
        double[] values = new double[this.getArrayLength()];
        this.getDoubles(values, 0, values.length);
        return values;
    }

    /**
     * Retrieves the values of this uniform as a double array and stores them in the specified array.
     *
     * @param fill The array to fill
     */
    default void getDoubles(double[] fill) {
        this.getDoubles(fill, 0, fill.length);
    }

    /**
     * Retrieves the values of this uniform as a double array and stores them in the specified array.
     *
     * @param dst    The array to fill
     * @param offset The position to start filling the array at
     * @param length The number of values to read
     */
    void getDoubles(double[] dst, int offset, int length);

    /**
     * Retrieves the value of this uniform as a long.
     *
     * @return The int value of this uniform
     */
    long getLong();

    /**
     * Retrieves the values of this uniform as a long array.
     *
     * @return A new array with the values of this uniform
     */
    default long[] getLongs() {
        long[] values = new long[this.getArrayLength()];
        this.getLongs(values, 0, values.length);
        return values;
    }

    /**
     * Retrieves the values of this uniform as a long array and stores them in the specified array.
     *
     * @param fill The array to fill
     */
    default void getLongs(long[] fill) {
        this.getLongs(fill, 0, fill.length);
    }

    /**
     * Retrieves the values of this uniform as a long array and stores them in the specified array.
     *
     * @param dst    The array to fill
     * @param offset The position to start filling the array at
     * @param length The number of values to read
     */
    void getLongs(long[] dst, int offset, int length);

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
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    void getVectori(CharSequence name, Vector2i... values);

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    void getVectori(CharSequence name, Vector3i... values);

    /**
     * Retrieves an array of vectors by the specified name.
     *
     * @param name   The name of the uniform to get
     * @param values The values to set
     */
    void getVectori(CharSequence name, Vector4i... values);

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
     * Retrieves a matrix4x4 by the specified name
     *
     * @param name  The name of the uniform to get
     * @param value The value to set
     */
    void getMatrix(CharSequence name, Matrix4f value);

    /**
     * Retrieves a matrix3x2 by the specified name
     *
     * @param name  The name of the uniform to get
     * @param value The value to set
     */
    void getMatrix(CharSequence name, Matrix3x2f value);

    /**
     * Retrieves a matrix4x3 by the specified name
     *
     * @param name  The name of the uniform to get
     * @param value The value to set
     */
    void getMatrix(CharSequence name, Matrix4x3f value);

    /**
     * Possible uniform types that can be used.
     */
    enum Type {
        /**
         * @since 2.0.0
         */
        SAMPLER(0) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform1iv(program, location, buffer.asIntBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform1iv(location, buffer.asIntBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        FLOAT(Float.BYTES) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform1fv(program, location, buffer.asFloatBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform1fv(location, buffer.asFloatBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        FLOAT_VEC2(Float.BYTES * 2) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform2fv(program, location, buffer.asFloatBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform2fv(location, buffer.asFloatBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        FLOAT_VEC3(Float.BYTES * 3) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform3fv(program, location, buffer.asFloatBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform3fv(location, buffer.asFloatBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        FLOAT_VEC4(Float.BYTES * 4) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform4fv(program, location, buffer.asFloatBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform4fv(location, buffer.asFloatBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        INT(Integer.BYTES) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform1iv(program, location, buffer.asIntBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform1iv(location, buffer.asIntBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        INT_VEC2(Integer.BYTES * 2) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform2iv(program, location, buffer.asIntBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform2iv(location, buffer.asIntBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        INT_VEC3(Integer.BYTES * 3) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform3iv(program, location, buffer.asIntBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform3iv(location, buffer.asIntBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        INT_VEC4(Integer.BYTES * 4) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform4iv(program, location, buffer.asIntBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform4iv(location, buffer.asIntBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        UNSIGNED_INT(Integer.BYTES) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform1uiv(program, location, buffer.asIntBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform1uiv(location, buffer.asIntBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        UNSIGNED_INT_VEC2(Integer.BYTES * 2) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform2uiv(program, location, buffer.asIntBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform2uiv(location, buffer.asIntBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        UNSIGNED_INT_VEC3(Integer.BYTES * 3) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform3uiv(program, location, buffer.asIntBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform3uiv(location, buffer.asIntBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        UNSIGNED_INT_VEC4(Integer.BYTES * 4) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform4uiv(program, location, buffer.asIntBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform4uiv(location, buffer.asIntBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        MATRIX2x2(Float.BYTES * 2 * 2) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix2fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix2fv(location, transpose, buffer.asFloatBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        MATRIX3x3(Float.BYTES * 3 * 3) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix3fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix3fv(location, transpose, buffer.asFloatBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        MATRIX4x4(Float.BYTES * 4 * 4) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix4fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix4fv(location, transpose, buffer.asFloatBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        MATRIX2x3(Float.BYTES * 2 * 3) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix2x3fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix2x3fv(location, transpose, buffer.asFloatBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        MATRIX3x2(Float.BYTES * 3 * 2) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix3x2fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix3x2fv(location, transpose, buffer.asFloatBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        MATRIX2x4(Float.BYTES * 2 * 4) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix2x4fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix2x4fv(location, transpose, buffer.asFloatBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        MATRIX4x2(Float.BYTES * 4 * 2) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix4x2fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix4x2fv(location, transpose, buffer.asFloatBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        MATRIX3x4(Float.BYTES * 3 * 4) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix3x4fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix3x4fv(location, transpose, buffer.asFloatBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        MATRIX4x3(Float.BYTES * 4 * 3) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix4x3fv(program, location, transpose, buffer.asFloatBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix4x3fv(location, transpose, buffer.asFloatBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },

        DOUBLE(Double.BYTES) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform1dv(program, location, buffer.asDoubleBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform1dv(location, buffer.asDoubleBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        DOUBLE_VEC2(Double.BYTES * 2) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform2dv(program, location, buffer.asDoubleBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform2dv(location, buffer.asDoubleBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        DOUBLE_VEC3(Double.BYTES * 3) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform3dv(program, location, buffer.asDoubleBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform3dv(location, buffer.asDoubleBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        DOUBLE_VEC4(Double.BYTES * 4) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform4dv(program, location, buffer.asDoubleBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform4dv(location, buffer.asDoubleBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        LONG(Long.BYTES) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderInt64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Integers are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform1i64vARB(program, location, buffer.asLongBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform1i64vARB(location, buffer.asLongBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        LONG_VEC2(Long.BYTES * 2) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderInt64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Integers are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform2i64vARB(program, location, buffer.asLongBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform2i64vARB(location, buffer.asLongBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        LONG_VEC3(Long.BYTES * 3) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderInt64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Integers are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform3i64vARB(program, location, buffer.asLongBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform3i64vARB(location, buffer.asLongBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        LONG_VEC4(Long.BYTES * 4) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderInt64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Integers are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform4i64vARB(program, location, buffer.asLongBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform4i64vARB(location, buffer.asLongBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        UNSIGNED_LONG(Long.BYTES) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderInt64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Integers are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform1ui64vARB(program, location, buffer.asLongBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform1ui64vARB(location, buffer.asLongBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        UNSIGNED_LONG_VEC2(Long.BYTES * 2) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderInt64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Integers are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform2ui64vARB(program, location, buffer.asLongBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform2ui64vARB(location, buffer.asLongBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        UNSIGNED_LONG_VEC3(Long.BYTES * 3) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderInt64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Integers are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform3ui64vARB(program, location, buffer.asLongBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform3ui64vARB(location, buffer.asLongBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        UNSIGNED_LONG_VEC4(Long.BYTES * 4) {
            @Override
            public void upload(int program, int location, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderInt64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Integers are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniform4ui64vARB(program, location, buffer.asLongBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniform4ui64vARB(location, buffer.asLongBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        DOUBLE_MATRIX2x2(Double.BYTES * 2 * 2) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix2dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix2dv(location, transpose, buffer.asDoubleBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        DOUBLE_MATRIX3x3(Double.BYTES * 3 * 3) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix3dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix3dv(location, transpose, buffer.asDoubleBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        DOUBLE_MATRIX4x4(Double.BYTES * 4 * 4) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix4dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix4dv(location, transpose, buffer.asDoubleBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        DOUBLE_MATRIX2x3(Double.BYTES * 2 * 3) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix2x3dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix2x3dv(location, transpose, buffer.asDoubleBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        DOUBLE_MATRIX3x2(Double.BYTES * 3 * 2) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix3x2dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix3x2dv(location, transpose, buffer.asDoubleBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        DOUBLE_MATRIX2x4(Double.BYTES * 2 * 4) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix2x4dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix2x4dv(location, transpose, buffer.asDoubleBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        DOUBLE_MATRIX4x2(Double.BYTES * 4 * 2) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix4x2dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix4x2dv(location, transpose, buffer.asDoubleBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        DOUBLE_MATRIX3x4(Double.BYTES * 3 * 4) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix3x4dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix3x4dv(location, transpose, buffer.asDoubleBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        },
        DOUBLE_MATRIX4x3(Double.BYTES * 4 * 3) {
            @Override
            public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
                if (!VeilRenderSystem.gpuShaderFloat64BitSupported()) {
                    throw new UnsupportedOperationException("64-Bit Floats are not supported");
                }
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformMatrix4x3dv(program, location, transpose, buffer.asDoubleBuffer());
                } else {
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(program);
                    }
                    glUniformMatrix4x3dv(location, transpose, buffer.asDoubleBuffer());
                    if (program != ShaderInstance.lastProgramId) {
                        glUseProgram(ShaderInstance.lastProgramId);
                    }
                }
            }
        };

        private final int bytes;

        Type(int bytes) {
            this.bytes = bytes;
        }

        public static Type byId(int type) {
            if (ShaderUniformCache.isSampler(type)) {
                return SAMPLER;
            }
            return switch (type) {
                case GL_FLOAT -> FLOAT;
                case GL_FLOAT_VEC2 -> FLOAT_VEC2;
                case GL_FLOAT_VEC3 -> FLOAT_VEC3;
                case GL_FLOAT_VEC4 -> FLOAT_VEC4;
                case GL_INT -> INT;
                case GL_INT_VEC2 -> INT_VEC2;
                case GL_INT_VEC3 -> INT_VEC3;
                case GL_INT_VEC4 -> INT_VEC4;
                case GL_UNSIGNED_INT -> UNSIGNED_INT;
                case GL_UNSIGNED_INT_VEC2 -> UNSIGNED_INT_VEC2;
                case GL_UNSIGNED_INT_VEC3 -> UNSIGNED_INT_VEC3;
                case GL_UNSIGNED_INT_VEC4 -> UNSIGNED_INT_VEC4;
                case GL_FLOAT_MAT2 -> MATRIX2x2;
                case GL_FLOAT_MAT3 -> MATRIX3x3;
                case GL_FLOAT_MAT4 -> MATRIX4x4;
                case GL_FLOAT_MAT2x3 -> MATRIX2x3;
                case GL_FLOAT_MAT3x4 -> MATRIX3x4;

                case GL_DOUBLE -> DOUBLE;
                case GL_DOUBLE_VEC2 -> DOUBLE_VEC2;
                case GL_DOUBLE_VEC3 -> DOUBLE_VEC3;
                case GL_DOUBLE_VEC4 -> DOUBLE_VEC4;
                case GL_INT64_ARB -> LONG;
                case GL_INT64_VEC2_ARB -> LONG_VEC2;
                case GL_INT64_VEC3_ARB -> LONG_VEC3;
                case GL_INT64_VEC4_ARB -> LONG_VEC4;
                case GL_UNSIGNED_INT64_ARB -> UNSIGNED_LONG;
                case GL_UNSIGNED_INT64_VEC2_ARB -> UNSIGNED_LONG_VEC2;
                case GL_UNSIGNED_INT64_VEC3_ARB -> UNSIGNED_LONG_VEC3;
                case GL_UNSIGNED_INT64_VEC4_ARB -> UNSIGNED_LONG_VEC4;
                case GL_DOUBLE_MAT2 -> DOUBLE_MATRIX2x2;
                case GL_DOUBLE_MAT3 -> DOUBLE_MATRIX3x3;
                case GL_DOUBLE_MAT4 -> DOUBLE_MATRIX4x4;
                case GL_DOUBLE_MAT2x3 -> DOUBLE_MATRIX2x3;
                case GL_DOUBLE_MAT3x4 -> DOUBLE_MATRIX3x4;
                default -> throw new AssertionError("Invalid Uniform Type: " + ShaderUniformCache.getName(type));
            };
        }

        public void upload(int program, int location, ByteBuffer buffer) {
            throw new UnsupportedOperationException();
        }

        public void uploadAsMatrix(int program, int location, boolean transpose, ByteBuffer buffer) {
            throw new UnsupportedOperationException();
        }

        public int getBytes() {
            if (this == SAMPLER) {
                return VeilRenderSystem.bindlessTextureSupported() ? Long.BYTES : Integer.BYTES;
            }
            return this.bytes;
        }
    }
}
