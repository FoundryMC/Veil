package foundry.veil.impl.client.render.shader.uniform;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.program.ShaderUniformCache;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;

import java.lang.Math;
import java.nio.*;
import java.util.Objects;
import java.util.function.IntSupplier;

import static org.lwjgl.opengl.ARBBindlessTexture.glProgramUniformHandleui64vARB;
import static org.lwjgl.opengl.ARBBindlessTexture.glUniformHandleui64vARB;
import static org.lwjgl.opengl.ARBGPUShaderFP64.glGetUniformd;
import static org.lwjgl.opengl.ARBGPUShaderFP64.glGetUniformdv;
import static org.lwjgl.opengl.ARBGPUShaderInt64.glGetnUniformi64vARB;
import static org.lwjgl.opengl.ARBGPUShaderInt64.glGetnUniformui64vARB;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glGetUniformui;
import static org.lwjgl.opengl.GL30C.glGetUniformuiv;

@ApiStatus.Internal
public class ShaderUniformImpl implements ShaderUniform, NativeResource {

    private final IntSupplier program;
    private final String name;

    private Type type;
    private int length;
    private ByteBuffer value;
    private int location;

    public ShaderUniformImpl(IntSupplier program, String name) {
        this.program = program;
        this.name = name;
        this.set(null);
    }

    private void upload() {
        this.type.upload(this.program.getAsInt(), this.location, this.value);
    }

    private void uploadMatrix(boolean transpose) {
        this.type.uploadAsMatrix(this.program.getAsInt(), this.location, transpose, this.value);
    }

    public void set(@Nullable ShaderUniformCache.Uniform uniform) {
        if (uniform != null) {
            this.type = Type.byId(uniform.type());
            this.length = uniform.arrayLength();
            if (this.value == null || this.value.capacity() != this.type.getBytes() * this.length) {
                this.value = MemoryUtil.memRealloc(this.value, this.type.getBytes() * this.length);
            }
            this.location = uniform.location();
            this.invalidateCache();
        } else {
            this.type = null;
            this.length = 0;
            this.free();
            this.location = -1;
        }
    }

    @Override
    public void invalidateCache() {
        if (this.value != null) {
            MemoryUtil.memSet(this.value, 0);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getLocation() {
        return this.location;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public int getArrayLength() {
        return this.length;
    }

    @Override
    public float getFloat() {
        return switch (this.type) {
            case FLOAT -> glGetUniformf(this.program.getAsInt(), this.location);
            case SAMPLER, INT -> glGetUniformi(this.program.getAsInt(), this.location);
            case UNSIGNED_INT -> glGetUniformui(this.program.getAsInt(), this.location);
            case DOUBLE -> (float) glGetUniformd(this.program.getAsInt(), this.location);
            case LONG -> glGetnUniformi64vARB(this.program.getAsInt(), this.location);
            case UNSIGNED_LONG -> glGetnUniformui64vARB(this.program.getAsInt(), this.location);
            default -> 0.0F;
        };
    }

    @Override
    public void getFloats(float[] dst, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, dst.length);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            switch (this.type) {
                case FLOAT -> {
                    FloatBuffer buffer = stack.mallocFloat(Math.min(this.length, length));
                    glGetUniformfv(this.program.getAsInt(), this.location, buffer);
                    buffer.get(dst);
                }
                case SAMPLER, INT -> {
                    IntBuffer buffer = stack.mallocInt(Math.min(this.length, length));
                    glGetUniformiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = buffer.get(i);
                    }
                }
                case UNSIGNED_INT -> {
                    IntBuffer buffer = stack.mallocInt(Math.min(this.length, length));
                    glGetUniformuiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = buffer.get(i);
                    }
                }
                case DOUBLE -> {
                    DoubleBuffer buffer = stack.mallocDouble(Math.min(this.length, length));
                    glGetUniformdv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = (float) buffer.get(i);
                    }
                }
                case LONG -> {
                    LongBuffer buffer = stack.mallocLong(Math.min(this.length, length));
                    glGetnUniformi64vARB(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = (int) buffer.get(i);
                    }
                }
                case UNSIGNED_LONG -> {
                    LongBuffer buffer = stack.mallocLong(Math.min(this.length, length));
                    glGetnUniformui64vARB(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = (int) buffer.get(i);
                    }
                }
                default -> {
                    for (int i = 0; i < Math.min(this.length, length); i++) {
                        dst[i + offset] = 0.0F;
                    }
                }
            }
        }
    }

    @Override
    public int getInt() {
        return switch (this.type) {
            case FLOAT -> (int) glGetUniformf(this.program.getAsInt(), this.location);
            case SAMPLER, INT -> glGetUniformi(this.program.getAsInt(), this.location);
            case UNSIGNED_INT -> glGetUniformui(this.program.getAsInt(), this.location);
            case DOUBLE -> (int) glGetUniformd(this.program.getAsInt(), this.location);
            case LONG -> (int) glGetnUniformi64vARB(this.program.getAsInt(), this.location);
            case UNSIGNED_LONG -> (int) glGetnUniformui64vARB(this.program.getAsInt(), this.location);
            default -> 0;
        };
    }

    @Override
    public void getInts(int[] dst, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, dst.length);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            switch (this.type) {
                case FLOAT -> {
                    FloatBuffer buffer = stack.mallocFloat(Math.min(this.length, length));
                    glGetUniformfv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = (int) buffer.get(i);
                    }
                }
                case SAMPLER, INT -> {
                    IntBuffer buffer = stack.mallocInt(Math.min(this.length, length));
                    glGetUniformiv(this.program.getAsInt(), this.location, buffer);
                    buffer.get(dst);
                }
                case UNSIGNED_INT -> {
                    IntBuffer buffer = stack.mallocInt(Math.min(this.length, length));
                    glGetUniformuiv(this.program.getAsInt(), this.location, buffer);
                    buffer.get(dst);
                }
                case DOUBLE -> {
                    DoubleBuffer buffer = stack.mallocDouble(Math.min(this.length, length));
                    glGetUniformdv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = (int) buffer.get(i);
                    }
                }
                case LONG -> {
                    LongBuffer buffer = stack.mallocLong(Math.min(this.length, length));
                    glGetnUniformi64vARB(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = (int) buffer.get(i);
                    }
                }
                case UNSIGNED_LONG -> {
                    LongBuffer buffer = stack.mallocLong(Math.min(this.length, length));
                    glGetnUniformui64vARB(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = (int) buffer.get(i);
                    }
                }
                default -> {
                    for (int i = 0; i < Math.min(this.length, length); i++) {
                        dst[i + offset] = 0;
                    }
                }
            }
        }
    }

    @Override
    public double getDouble() {
        return switch (this.type) {
            case FLOAT -> glGetUniformf(this.program.getAsInt(), this.location);
            case SAMPLER, INT -> glGetUniformi(this.program.getAsInt(), this.location);
            case UNSIGNED_INT -> glGetUniformui(this.program.getAsInt(), this.location);
            case DOUBLE -> glGetUniformd(this.program.getAsInt(), this.location);
            case LONG -> glGetnUniformi64vARB(this.program.getAsInt(), this.location);
            case UNSIGNED_LONG -> glGetnUniformui64vARB(this.program.getAsInt(), this.location);
            default -> 0.0F;
        };
    }

    @Override
    public void getDoubles(double[] dst, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, dst.length);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            switch (this.type) {
                case FLOAT -> {
                    FloatBuffer buffer = stack.mallocFloat(Math.min(this.length, length));
                    glGetUniformfv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = buffer.get(i);
                    }
                }
                case SAMPLER, INT -> {
                    IntBuffer buffer = stack.mallocInt(Math.min(this.length, length));
                    glGetUniformiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = buffer.get(i);
                    }
                }
                case UNSIGNED_INT -> {
                    IntBuffer buffer = stack.mallocInt(Math.min(this.length, length));
                    glGetUniformuiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = buffer.get(i);
                    }
                }
                case DOUBLE -> {
                    DoubleBuffer buffer = stack.mallocDouble(Math.min(this.length, length));
                    glGetUniformdv(this.program.getAsInt(), this.location, buffer);
                    buffer.get(dst);
                }
                case LONG -> {
                    LongBuffer buffer = stack.mallocLong(Math.min(this.length, length));
                    glGetnUniformi64vARB(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = buffer.get(i);
                    }
                }
                case UNSIGNED_LONG -> {
                    LongBuffer buffer = stack.mallocLong(Math.min(this.length, length));
                    glGetnUniformui64vARB(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = buffer.get(i);
                    }
                }
                default -> {
                    for (int i = 0; i < Math.min(this.length, length); i++) {
                        dst[i + offset] = 0;
                    }
                }
            }
        }
    }

    @Override
    public long getLong() {
        return switch (this.type) {
            case FLOAT -> (long) glGetUniformf(this.program.getAsInt(), this.location);
            case SAMPLER, INT -> glGetUniformi(this.program.getAsInt(), this.location);
            case UNSIGNED_INT -> glGetUniformui(this.program.getAsInt(), this.location);
            case DOUBLE -> (long) glGetUniformd(this.program.getAsInt(), this.location);
            case LONG -> glGetnUniformi64vARB(this.program.getAsInt(), this.location);
            case UNSIGNED_LONG -> glGetnUniformui64vARB(this.program.getAsInt(), this.location);
            default -> 0L;
        };
    }

    @Override
    public void getLongs(long[] dst, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, dst.length);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            switch (this.type) {
                case FLOAT -> {
                    FloatBuffer buffer = stack.mallocFloat(Math.min(this.length, length));
                    glGetUniformfv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = (long) buffer.get(i);
                    }
                }
                case SAMPLER, INT -> {
                    IntBuffer buffer = stack.mallocInt(Math.min(this.length, length));
                    glGetUniformiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = buffer.get(i);
                    }
                }
                case UNSIGNED_INT -> {
                    IntBuffer buffer = stack.mallocInt(Math.min(this.length, length));
                    glGetUniformuiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = buffer.get(i);
                    }
                }
                case DOUBLE -> {
                    DoubleBuffer buffer = stack.mallocDouble(Math.min(this.length, length));
                    glGetUniformdv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < buffer.capacity(); i++) {
                        dst[i + offset] = (long) buffer.get(i);
                    }
                }
                case LONG -> {
                    LongBuffer buffer = stack.mallocLong(Math.min(this.length, length));
                    glGetnUniformi64vARB(this.program.getAsInt(), this.location, buffer);
                    buffer.get(dst);
                }
                case UNSIGNED_LONG -> {
                    LongBuffer buffer = stack.mallocLong(Math.min(this.length, length));
                    glGetnUniformui64vARB(this.program.getAsInt(), this.location, buffer);
                    buffer.get(dst);
                }
                default -> {
                    for (int i = 0; i < Math.min(this.length, length); i++) {
                        dst[i + offset] = 0;
                    }
                }
            }
        }
    }

    @Override
    public void getVector(CharSequence name, Vector2f... values) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            switch (this.type) {
                case FLOAT_VEC2 -> {
                    FloatBuffer buffer = stack.mallocFloat(2 * Math.min(this.length, values.length));
                    glGetUniformfv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(i * 2, buffer);
                    }
                }
                case INT_VEC2 -> {
                    IntBuffer buffer = stack.mallocInt(2 * Math.min(this.length, values.length));
                    glGetUniformiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(buffer.get(i * 2), buffer.get(i * 2 + 1));
                    }
                }
                case UNSIGNED_INT_VEC2 -> {
                    IntBuffer buffer = stack.mallocInt(2 * Math.min(this.length, values.length));
                    glGetUniformuiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(buffer.get(i * 2), buffer.get(i * 2 + 1));
                    }
                }
                case DOUBLE_VEC2 -> {
                    DoubleBuffer buffer = stack.mallocDouble(2 * Math.min(this.length, values.length));
                    glGetUniformdv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(buffer.get(i * 2), buffer.get(i * 2 + 1));
                    }
                }
                default -> {
                    for (int i = 0; i < Math.min(this.length, values.length); i++) {
                        values[i].set(0.0F);
                    }
                }
            }
        }
    }

    @Override
    public void getVector(CharSequence name, Vector3f... values) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            switch (this.type) {
                case FLOAT_VEC3 -> {
                    FloatBuffer buffer = stack.mallocFloat(3 * Math.min(this.length, values.length));
                    glGetUniformfv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(i * 3, buffer);
                    }
                }
                case INT_VEC3 -> {
                    IntBuffer buffer = stack.mallocInt(3 * Math.min(this.length, values.length));
                    glGetUniformiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(buffer.get(i * 3), buffer.get(i * 3 + 1), buffer.get(i * 3 + 2));
                    }
                }
                case UNSIGNED_INT_VEC3 -> {
                    IntBuffer buffer = stack.mallocInt(3 * Math.min(this.length, values.length));
                    glGetUniformuiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(buffer.get(i * 3), buffer.get(i * 3 + 1), buffer.get(i * 3 + 2));
                    }
                }
                case DOUBLE_VEC3 -> {
                    DoubleBuffer buffer = stack.mallocDouble(3 * Math.min(this.length, values.length));
                    glGetUniformdv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(buffer.get(i * 3), buffer.get(i * 3 + 1), buffer.get(i * 3 + 2));
                    }
                }
                default -> {
                    for (int i = 0; i < Math.min(this.length, values.length); i++) {
                        values[i].set(0.0F);
                    }
                }
            }
        }
    }

    @Override
    public void getVector(CharSequence name, Vector4f... values) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            switch (this.type) {
                case FLOAT_VEC4 -> {
                    FloatBuffer buffer = stack.mallocFloat(4 * Math.min(this.length, values.length));
                    glGetUniformfv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(i * 4, buffer);
                    }
                }
                case INT_VEC4 -> {
                    IntBuffer buffer = stack.mallocInt(4 * Math.min(this.length, values.length));
                    glGetUniformiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(buffer.get(i * 4), buffer.get(i * 4 + 1), buffer.get(i * 4 + 2), buffer.get(i * 4 + 3));
                    }
                }
                case UNSIGNED_INT_VEC4 -> {
                    IntBuffer buffer = stack.mallocInt(4 * Math.min(this.length, values.length));
                    glGetUniformuiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(buffer.get(i * 4), buffer.get(i * 4 + 1), buffer.get(i * 4 + 2), buffer.get(i * 4 + 3));
                    }
                }
                case DOUBLE_VEC4 -> {
                    DoubleBuffer buffer = stack.mallocDouble(4 * Math.min(this.length, values.length));
                    glGetUniformdv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(buffer.get(i * 4), buffer.get(i * 4 + 1), buffer.get(i * 4 + 2), buffer.get(i * 4 + 3));
                    }
                }
                default -> {
                    for (int i = 0; i < Math.min(this.length, values.length); i++) {
                        values[i].set(0.0F);
                    }
                }
            }
        }
    }

    @Override
    public void getVectori(CharSequence name, Vector2i... values) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            switch (this.type) {
                case FLOAT_VEC2 -> {
                    FloatBuffer buffer = stack.mallocFloat(2 * Math.min(this.length, values.length));
                    glGetUniformfv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set((int) buffer.get(i * 2), (int) buffer.get(i * 2 + 1));
                    }
                }
                case INT_VEC2 -> {
                    IntBuffer buffer = stack.mallocInt(2 * Math.min(this.length, values.length));
                    glGetUniformiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(i * 2, buffer);
                    }
                }
                case UNSIGNED_INT_VEC2 -> {
                    IntBuffer buffer = stack.mallocInt(2 * Math.min(this.length, values.length));
                    glGetUniformuiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(i * 2, buffer);
                    }
                }
                case DOUBLE_VEC2 -> {
                    DoubleBuffer buffer = stack.mallocDouble(2 * Math.min(this.length, values.length));
                    glGetUniformdv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set((int) buffer.get(i * 2), (int) buffer.get(i * 2 + 1));
                    }
                }
                default -> {
                    for (int i = 0; i < Math.min(this.length, values.length); i++) {
                        values[i].set(0);
                    }
                }
            }
        }
    }

    @Override
    public void getVectori(CharSequence name, Vector3i... values) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            switch (this.type) {
                case FLOAT_VEC3 -> {
                    FloatBuffer buffer = stack.mallocFloat(3 * Math.min(this.length, values.length));
                    glGetUniformfv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set((int) buffer.get(i * 3), (int) buffer.get(i * 3 + 1), (int) buffer.get(i * 3 + 2));
                    }
                }
                case INT_VEC3 -> {
                    IntBuffer buffer = stack.mallocInt(3 * Math.min(this.length, values.length));
                    glGetUniformiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(i * 3, buffer);
                    }
                }
                case UNSIGNED_INT_VEC3 -> {
                    IntBuffer buffer = stack.mallocInt(3 * Math.min(this.length, values.length));
                    glGetUniformuiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(i * 3, buffer);
                    }
                }
                case DOUBLE_VEC3 -> {
                    DoubleBuffer buffer = stack.mallocDouble(3 * Math.min(this.length, values.length));
                    glGetUniformdv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set((int) buffer.get(i * 3), (int) buffer.get(i * 3 + 1), (int) buffer.get(i * 3 + 2));
                    }
                }
                default -> {
                    for (int i = 0; i < Math.min(this.length, values.length); i++) {
                        values[i].set(0);
                    }
                }
            }
        }
    }

    @Override
    public void getVectori(CharSequence name, Vector4i... values) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            switch (this.type) {
                case FLOAT_VEC4 -> {
                    FloatBuffer buffer = stack.mallocFloat(3 * Math.min(this.length, values.length));
                    glGetUniformfv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set((int) buffer.get(i * 4), (int) buffer.get(i * 4 + 1), (int) buffer.get(i * 4 + 2), (int) buffer.get(i * 4 + 3));
                    }
                }
                case INT_VEC4 -> {
                    IntBuffer buffer = stack.mallocInt(3 * Math.min(this.length, values.length));
                    glGetUniformiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(i * 4, buffer);
                    }
                }
                case UNSIGNED_INT_VEC4 -> {
                    IntBuffer buffer = stack.mallocInt(3 * Math.min(this.length, values.length));
                    glGetUniformuiv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set(i * 4, buffer);
                    }
                }
                case DOUBLE_VEC4 -> {
                    DoubleBuffer buffer = stack.mallocDouble(4 * Math.min(this.length, values.length));
                    glGetUniformdv(this.program.getAsInt(), this.location, buffer);
                    for (int i = 0; i < values.length; i++) {
                        values[i].set((int) buffer.get(i * 4), (int) buffer.get(i * 4 + 1), (int) buffer.get(i * 4 + 2), (int) buffer.get(i * 4 + 3));
                    }
                }
                default -> {
                    for (int i = 0; i < Math.min(this.length, values.length); i++) {
                        values[i].set(0);
                    }
                }
            }
        }
    }

    @Override
    public void getMatrix(CharSequence name, Matrix2f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(2 * 2);
            glGetUniformfv(this.program.getAsInt(), this.location, buffer);
            value.set(0, buffer);
        }
    }

    @Override
    public void getMatrix(CharSequence name, Matrix3f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3 * 3);
            glGetUniformfv(this.program.getAsInt(), this.location, buffer);
            value.set(0, buffer);
        }
    }

    @Override
    public void getMatrix(CharSequence name, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4 * 4);
            glGetUniformfv(this.program.getAsInt(), this.location, buffer);
            value.set(0, buffer);
        }
    }

    @Override
    public void getMatrix(CharSequence name, Matrix3x2f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3 * 2);
            glGetUniformfv(this.program.getAsInt(), this.location, buffer);
            value.set(0, buffer);
        }
    }

    @Override
    public void getMatrix(CharSequence name, Matrix4x3f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4 * 3);
            glGetUniformfv(this.program.getAsInt(), this.location, buffer);
            value.set(0, buffer);
        }
    }

    @Override
    public void free() {
        if (this.value != null) {
            MemoryUtil.memFree(this.value);
            this.value = null;
        }
    }

    @Override
    public void setFloat(float value) {
        if (this.type != Type.FLOAT ||
                this.value.getInt(0) == Float.floatToIntBits(value)) {
            return;
        }

        this.value.putFloat(0, value);
        this.upload();
    }

    @Override
    public void setVector(float x, float y) {
        if (this.type != Type.FLOAT_VEC2 ||
                (this.value.getInt(0) == Float.floatToIntBits(x) &&
                        this.value.getInt(4) == Float.floatToIntBits(y))) {
            return;
        }

        this.value.putFloat(0, x);
        this.value.putFloat(4, y);
        this.upload();
    }

    @Override
    public void setVector(float x, float y, float z) {
        if (this.type != Type.FLOAT_VEC3 ||
                (this.value.getInt(0) == Float.floatToIntBits(x) &&
                        this.value.getInt(4) == Float.floatToIntBits(y) &&
                        this.value.getInt(8) == Float.floatToIntBits(z))) {
            return;
        }

        this.value.putFloat(0, x);
        this.value.putFloat(4, y);
        this.value.putFloat(8, z);
        this.upload();
    }

    @Override
    public void setVector(float x, float y, float z, float w) {
        if (this.type != Type.FLOAT_VEC4 ||
                (this.value.getInt(0) == Float.floatToIntBits(x) &&
                        this.value.getInt(4) == Float.floatToIntBits(y) &&
                        this.value.getInt(8) == Float.floatToIntBits(z) &&
                        this.value.getInt(12) == Float.floatToIntBits(w))) {
            return;
        }

        this.value.putFloat(0, x);
        this.value.putFloat(4, y);
        this.value.putFloat(8, z);
        this.value.putFloat(12, w);
        this.upload();
    }

    @Override
    public void setInt(int value) {
        if ((this.type != Type.INT && this.type != Type.UNSIGNED_INT && this.type != Type.SAMPLER) ||
                this.value.getInt(0) == value) {
            return;
        }

        if (this.type == Type.SAMPLER) {
            this.value.limit(Integer.BYTES * this.length);
        }

        this.value.putInt(0, value);
        this.upload();
    }

    @Override
    public void setVectorI(int x, int y) {
        if ((this.type != Type.INT_VEC2 && this.type != Type.UNSIGNED_INT_VEC2) ||
                (this.value.getInt(0) == x &&
                        this.value.getInt(4) == y)) {
            return;
        }

        this.value.putInt(0, x);
        this.value.putInt(4, y);
        this.upload();
    }

    @Override
    public void setVectorI(int x, int y, int z) {
        if ((this.type != Type.INT_VEC3 && this.type != Type.UNSIGNED_INT_VEC3) ||
                (this.value.getInt(0) == x &&
                        this.value.getInt(4) == y &&
                        this.value.getInt(8) == z)) {
            return;
        }

        this.value.putInt(0, x);
        this.value.putInt(4, y);
        this.value.putInt(8, z);
        this.upload();
    }

    @Override
    public void setVectorI(int x, int y, int z, int w) {
        if ((this.type != Type.INT_VEC4 && this.type != Type.UNSIGNED_INT_VEC4) ||
                (this.value.getInt(0) == x &&
                        this.value.getInt(4) == y &&
                        this.value.getInt(8) == z &&
                        this.value.getInt(12) == w)) {
            return;
        }

        this.value.putInt(0, x);
        this.value.putInt(4, y);
        this.value.putInt(8, z);
        this.value.putInt(12, w);
        this.upload();
    }

    @Override
    public void setDouble(double value) {
        if (this.type != Type.DOUBLE ||
                this.value.getLong(0) == Double.doubleToLongBits(value)) {
            return;
        }

        this.value.putDouble(0, value);
        this.upload();
    }

    @Override
    public void setVector64(double x, double y) {
        if (this.type != Type.DOUBLE_VEC2 ||
                (this.value.getLong(0) == Double.doubleToLongBits(x) &&
                        this.value.getLong(8) == Double.doubleToLongBits(y))) {
            return;
        }

        this.value.putDouble(0, x);
        this.value.putDouble(8, y);
        this.upload();
    }

    @Override
    public void setVector64(double x, double y, double z) {
        if (this.type != Type.DOUBLE_VEC3 ||
                (this.value.getLong(0) == Double.doubleToLongBits(x) &&
                        this.value.getLong(8) == Double.doubleToLongBits(y) &&
                        this.value.getLong(16) == Double.doubleToLongBits(z))) {
            return;
        }

        this.value.putDouble(0, x);
        this.value.putDouble(8, y);
        this.value.putDouble(16, z);
        this.upload();
    }

    @Override
    public void setVector64(double x, double y, double z, double w) {
        if (this.type != Type.DOUBLE_VEC4 ||
                (this.value.getLong(0) == Double.doubleToLongBits(x) &&
                        this.value.getLong(8) == Double.doubleToLongBits(y) &&
                        this.value.getLong(16) == Double.doubleToLongBits(z) &&
                        this.value.getLong(24) == Double.doubleToLongBits(w))) {
            return;
        }

        this.value.putDouble(0, x);
        this.value.putDouble(8, y);
        this.value.putDouble(16, z);
        this.value.putDouble(24, w);
        this.upload();
    }

    @Override
    public void setLong(long value) {
        if ((this.type != Type.LONG && this.type != Type.UNSIGNED_LONG) ||
                this.value.getLong(0) == value) {
            return;
        }

        this.value.putLong(0, value);
        this.upload();
    }

    @Override
    public void setVectorI64(long x, long y) {
        if ((this.type != Type.LONG_VEC2 && this.type != Type.UNSIGNED_LONG_VEC2) ||
                (this.value.getLong(0) == x &&
                        this.value.getLong(8) == y)) {
            return;
        }

        this.value.putLong(0, x);
        this.value.putLong(8, y);
        this.upload();
    }

    @Override
    public void setVectorI64(long x, long y, long z) {
        if ((this.type != Type.LONG_VEC3 && this.type != Type.UNSIGNED_LONG_VEC3) ||
                (this.value.getLong(0) == x &&
                        this.value.getLong(8) == y &&
                        this.value.getLong(16) == z)) {
            return;
        }

        this.value.putLong(0, x);
        this.value.putLong(8, y);
        this.value.putLong(16, z);
        this.upload();
    }

    @Override
    public void setVectorI64(long x, long y, long z, long w) {
        if ((this.type != Type.LONG_VEC4 && this.type != Type.UNSIGNED_LONG_VEC4) ||
                (this.value.getLong(0) == x &&
                        this.value.getLong(8) == y &&
                        this.value.getLong(16) == z &&
                        this.value.getLong(24) == w)) {
            return;
        }

        this.value.putLong(0, x);
        this.value.putLong(8, y);
        this.value.putLong(16, z);
        this.value.putLong(24, w);
        this.upload();
    }

    @Override
    public void setFloats(float... values) {
        if (this.type != Type.FLOAT && this.type != Type.FLOAT_VEC2 && this.type != Type.FLOAT_VEC3 && this.type != Type.FLOAT_VEC4) {
            return;
        }

        int length = Math.min(this.value.capacity() / this.type.getBytes(), values.length);
        for (int i = 0; i < length; i++) {
            if (this.value.getInt(i * 4) != Float.floatToIntBits(values[i])) {
                this.value.asFloatBuffer().put(0, values, 0, length);
                this.upload();
                break;
            }
        }
    }

    @Override
    public void setVectors(Vector2fc... values) {
        if (this.type != Type.FLOAT_VEC2) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector2fc vector = values[i];
            if (vector.equals(this.value.getFloat(i * 8), this.value.getFloat(i * 8 + 4))) {
                changed = true;
            }
            vector.get(i * 8, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void setVectors(Vector3fc... values) {
        if (this.type != Type.FLOAT_VEC3) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector3fc vector = values[i];
            if (vector.equals(this.value.getFloat(i * 12), this.value.getFloat(i * 12 + 4), this.value.getFloat(i * 12 + 8))) {
                changed = true;
            }
            vector.get(i * 12, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void setVectors(Vector4fc... values) {
        if (this.type != Type.FLOAT_VEC4) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector4fc vector = values[i];
            if (vector.equals(this.value.getFloat(i * 16), this.value.getFloat(i * 16 + 4), this.value.getFloat(i * 16 + 8), this.value.getFloat(i * 16 + 12))) {
                changed = true;
            }
            vector.get(i * 16, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void setInts(int... values) {
        if (this.type != Type.INT && this.type != Type.INT_VEC2 && this.type != Type.INT_VEC3 && this.type != Type.INT_VEC4 &&
                this.type != Type.UNSIGNED_INT && this.type != Type.UNSIGNED_INT_VEC2 && this.type != Type.UNSIGNED_INT_VEC3 && this.type != Type.UNSIGNED_INT_VEC4 &&
                this.type != Type.SAMPLER) {
            return;
        }

        int length = Math.min(this.value.capacity() / (this.type == Type.SAMPLER ? Integer.BYTES : this.type.getBytes()), values.length);
        for (int i = 0; i < length; i++) {
            if (this.value.getInt(i * 4) != values[i]) {
                if (this.type == Type.SAMPLER) {
                    this.value.limit(Integer.BYTES * this.length);
                }
                this.value.asIntBuffer().put(0, values, 0, length);
                this.upload();
                break;
            }
        }
    }

    @Override
    public void setIVectors(Vector2ic... values) {
        if (this.type != Type.INT_VEC2 && this.type != Type.UNSIGNED_INT_VEC2) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector2ic vector = values[i];
            if (vector.equals(this.value.getInt(i * 8), this.value.getInt(i * 8 + 4))) {
                changed = true;
            }
            vector.get(i * 8, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void setIVectors(Vector3ic... values) {
        if (this.type != Type.INT_VEC3 && this.type != Type.UNSIGNED_INT_VEC3) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector3ic vector = values[i];
            if (vector.equals(this.value.getInt(i * 12), this.value.getInt(i * 12 + 4), this.value.getInt(i * 12 + 8))) {
                changed = true;
            }
            vector.get(i * 12, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void setIVectors(Vector4ic... values) {
        if (this.type != Type.INT_VEC4 && this.type != Type.UNSIGNED_INT_VEC4) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector4ic vector = values[i];
            if (vector.equals(this.value.getInt(i * 16), this.value.getInt(i * 16 + 4), this.value.getInt(i * 16 + 8), this.value.getInt(i * 16 + 12))) {
                changed = true;
            }
            vector.get(i * 16, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void setDoubles(double... values) {
        if (this.type != Type.DOUBLE && this.type != Type.DOUBLE_VEC2 && this.type != Type.DOUBLE_VEC3 && this.type != Type.DOUBLE_VEC4) {
            return;
        }

        int length = Math.min(this.value.capacity() / this.type.getBytes(), values.length);
        for (int i = 0; i < length; i++) {
            if (this.value.getLong(i * 8) != Double.doubleToLongBits(values[i])) {
                this.value.asDoubleBuffer().put(0, values, 0, length);
                this.upload();
                break;
            }
        }
    }

    @Override
    public void set64Vectors(Vector2dc... values) {
        if (this.type != Type.DOUBLE_VEC2) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector2dc vector = values[i];
            if (vector.equals(this.value.getDouble(i * 16), this.value.getDouble(i * 16 + 8))) {
                changed = true;
            }
            vector.get(i * 16, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void set64Vectors(Vector3dc... values) {
        if (this.type != Type.DOUBLE_VEC3) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector3dc vector = values[i];
            if (vector.equals(this.value.getDouble(i * 24), this.value.getDouble(i * 24 + 8), this.value.getDouble(i * 24 + 16))) {
                changed = true;
            }
            vector.get(i * 24, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void set64Vectors(Vector4dc... values) {
        if (this.type != Type.DOUBLE_VEC4) {
            return;
        }

        int length = Math.min(this.length, values.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            Vector4dc vector = values[i];
            if (vector.equals(this.value.getDouble(i * 32), this.value.getDouble(i * 32 + 8), this.value.getDouble(i * 32 + 16), this.value.getDouble(i * 32 + 24))) {
                changed = true;
            }
            vector.get(i * 32, this.value);
        }
        if (changed) {
            this.upload();
        }
    }

    @Override
    public void setLongs(long... values) {
        if (this.type != Type.LONG && this.type != Type.LONG_VEC2 && this.type != Type.LONG_VEC3 && this.type != Type.LONG_VEC4 &&
                this.type != Type.UNSIGNED_LONG && this.type != Type.UNSIGNED_LONG_VEC2 && this.type != Type.UNSIGNED_LONG_VEC3 && this.type != Type.UNSIGNED_LONG_VEC4) {
            return;
        }

        int length = Math.min(this.value.capacity() / this.type.getBytes(), values.length);
        for (int i = 0; i < length; i++) {
            if (this.value.getLong(i * 8) != values[i]) {
                this.value.asLongBuffer().put(0, values, 0, length);
                this.upload();
                break;
            }
        }
    }

    @Override
    public void setHandle(long value) {
        if (this.type != Type.SAMPLER || !VeilRenderSystem.bindlessTextureSupported() ||
                this.value.getLong(0) == value) {
            return;
        }

        this.value.limit(this.type.getBytes() * this.length);
        this.value.putLong(0, value);

        if (VeilRenderSystem.separateShaderObjectsSupported()) {
            glProgramUniformHandleui64vARB(this.program.getAsInt(), this.location, this.value.asLongBuffer());
        } else {
            glUniformHandleui64vARB(this.location, this.value.asLongBuffer());
        }
    }

    @Override
    public void setHandles(long... values) {
        if (this.type != Type.SAMPLER || !VeilRenderSystem.bindlessTextureSupported()) {
            return;
        }

        int length = Math.min(this.value.capacity() / this.type.getBytes(), values.length);
        for (int i = 0; i < length; i++) {
            if (this.value.getLong(i * 8) != values[i]) {
                this.value.limit(this.type.getBytes() * this.length);

                LongBuffer longBuffer = this.value.asLongBuffer();
                longBuffer.put(0, values, 0, length);
                if (VeilRenderSystem.separateShaderObjectsSupported()) {
                    glProgramUniformHandleui64vARB(this.program.getAsInt(), this.location, longBuffer);
                } else {
                    glUniformHandleui64vARB(this.location, longBuffer);
                }
                break;
            }
        }
    }

    @Override
    public void setMatrix(Matrix2fc value, boolean transpose) {
        if (this.type != Type.MATRIX2x2) {
            return;
        }

        if (this.value.getInt(0) == Float.floatToIntBits(value.m00()) &&
                this.value.getInt(4) == Float.floatToIntBits(value.m01()) &&
                this.value.getInt(8) == Float.floatToIntBits(value.m10()) &&
                this.value.getInt(12) == Float.floatToIntBits(value.m11())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix(Matrix3fc value, boolean transpose) {
        if (this.type != Type.MATRIX3x3) {
            return;
        }

        if (this.value.getInt(0) == Float.floatToIntBits(value.m00()) &&
                this.value.getInt(4) == Float.floatToIntBits(value.m01()) &&
                this.value.getInt(8) == Float.floatToIntBits(value.m02()) &&
                this.value.getInt(12) == Float.floatToIntBits(value.m10()) &&
                this.value.getInt(16) == Float.floatToIntBits(value.m11()) &&
                this.value.getInt(20) == Float.floatToIntBits(value.m12()) &&
                this.value.getInt(24) == Float.floatToIntBits(value.m20()) &&
                this.value.getInt(28) == Float.floatToIntBits(value.m21()) &&
                this.value.getInt(32) == Float.floatToIntBits(value.m22())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix(Matrix4fc value, boolean transpose) {
        if (this.type != Type.MATRIX4x4) {
            return;
        }

        if (this.value.getInt(0) == Float.floatToIntBits(value.m00()) &&
                this.value.getInt(4) == Float.floatToIntBits(value.m01()) &&
                this.value.getInt(8) == Float.floatToIntBits(value.m02()) &&
                this.value.getInt(12) == Float.floatToIntBits(value.m03()) &&
                this.value.getInt(16) == Float.floatToIntBits(value.m10()) &&
                this.value.getInt(20) == Float.floatToIntBits(value.m11()) &&
                this.value.getInt(24) == Float.floatToIntBits(value.m12()) &&
                this.value.getInt(28) == Float.floatToIntBits(value.m13()) &&
                this.value.getInt(32) == Float.floatToIntBits(value.m20()) &&
                this.value.getInt(36) == Float.floatToIntBits(value.m21()) &&
                this.value.getInt(40) == Float.floatToIntBits(value.m22()) &&
                this.value.getInt(44) == Float.floatToIntBits(value.m23()) &&
                this.value.getInt(48) == Float.floatToIntBits(value.m30()) &&
                this.value.getInt(52) == Float.floatToIntBits(value.m31()) &&
                this.value.getInt(56) == Float.floatToIntBits(value.m32()) &&
                this.value.getInt(60) == Float.floatToIntBits(value.m33())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix2x3(Matrix3x2fc value, boolean transpose) {
        if (this.type != Type.MATRIX2x3) {
            return;
        }

        if (this.value.getInt(0) == Float.floatToIntBits(value.m00()) &&
                this.value.getInt(4) == Float.floatToIntBits(value.m01()) &&
                this.value.getInt(8) == Float.floatToIntBits(value.m10()) &&
                this.value.getInt(16) == Float.floatToIntBits(value.m11()) &&
                this.value.getInt(20) == Float.floatToIntBits(value.m20()) &&
                this.value.getInt(24) == Float.floatToIntBits(value.m21())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix3x2(Matrix3x2fc value, boolean transpose) {
        if (this.type != Type.MATRIX3x2) {
            return;
        }

        if (this.value.getInt(0) == Float.floatToIntBits(value.m00()) &&
                this.value.getInt(4) == Float.floatToIntBits(value.m01()) &&
                this.value.getInt(8) == Float.floatToIntBits(value.m10()) &&
                this.value.getInt(16) == Float.floatToIntBits(value.m11()) &&
                this.value.getInt(20) == Float.floatToIntBits(value.m20()) &&
                this.value.getInt(24) == Float.floatToIntBits(value.m21())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(!transpose);
    }

    @Override
    public void setMatrix3x4(Matrix4x3fc value, boolean transpose) {
        if (this.type != Type.MATRIX3x4) {
            return;
        }

        if (this.value.getInt(0) == Float.floatToIntBits(value.m00()) &&
                this.value.getInt(4) == Float.floatToIntBits(value.m01()) &&
                this.value.getInt(8) == Float.floatToIntBits(value.m02()) &&
                this.value.getInt(12) == Float.floatToIntBits(value.m10()) &&
                this.value.getInt(16) == Float.floatToIntBits(value.m11()) &&
                this.value.getInt(20) == Float.floatToIntBits(value.m12()) &&
                this.value.getInt(24) == Float.floatToIntBits(value.m20()) &&
                this.value.getInt(28) == Float.floatToIntBits(value.m21()) &&
                this.value.getInt(32) == Float.floatToIntBits(value.m22()) &&
                this.value.getInt(36) == Float.floatToIntBits(value.m30()) &&
                this.value.getInt(40) == Float.floatToIntBits(value.m31()) &&
                this.value.getInt(44) == Float.floatToIntBits(value.m32())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(!transpose);
    }

    @Override
    public void setMatrix4x3(Matrix4x3fc value, boolean transpose) {
        if (this.type != Type.MATRIX4x3) {
            return;
        }

        if (this.value.getInt(0) == Float.floatToIntBits(value.m00()) &&
                this.value.getInt(4) == Float.floatToIntBits(value.m01()) &&
                this.value.getInt(8) == Float.floatToIntBits(value.m02()) &&
                this.value.getInt(12) == Float.floatToIntBits(value.m10()) &&
                this.value.getInt(16) == Float.floatToIntBits(value.m11()) &&
                this.value.getInt(20) == Float.floatToIntBits(value.m12()) &&
                this.value.getInt(24) == Float.floatToIntBits(value.m20()) &&
                this.value.getInt(28) == Float.floatToIntBits(value.m21()) &&
                this.value.getInt(32) == Float.floatToIntBits(value.m22()) &&
                this.value.getInt(36) == Float.floatToIntBits(value.m30()) &&
                this.value.getInt(40) == Float.floatToIntBits(value.m31()) &&
                this.value.getInt(44) == Float.floatToIntBits(value.m32())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix(Matrix2dc value, boolean transpose) {
        if (this.type != Type.MATRIX2x2) {
            return;
        }

        if (this.value.getLong(0) == Double.doubleToLongBits(value.m00()) &&
                this.value.getLong(8) == Double.doubleToLongBits(value.m01()) &&
                this.value.getLong(16) == Double.doubleToLongBits(value.m10()) &&
                this.value.getLong(24) == Double.doubleToLongBits(value.m11())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix(Matrix3dc value, boolean transpose) {
        if (this.type != Type.MATRIX3x3) {
            return;
        }

        if (this.value.getLong(0) == Double.doubleToLongBits(value.m00()) &&
                this.value.getLong(8) == Double.doubleToLongBits(value.m01()) &&
                this.value.getLong(16) == Double.doubleToLongBits(value.m02()) &&
                this.value.getLong(24) == Double.doubleToLongBits(value.m10()) &&
                this.value.getLong(32) == Double.doubleToLongBits(value.m11()) &&
                this.value.getLong(40) == Double.doubleToLongBits(value.m12()) &&
                this.value.getLong(48) == Double.doubleToLongBits(value.m20()) &&
                this.value.getLong(56) == Double.doubleToLongBits(value.m21()) &&
                this.value.getLong(64) == Double.doubleToLongBits(value.m22())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix(Matrix4dc value, boolean transpose) {
        if (this.type != Type.DOUBLE_MATRIX4x4) {
            return;
        }

        if (this.value.getLong(0) == Double.doubleToLongBits(value.m00()) &&
                this.value.getLong(8) == Double.doubleToLongBits(value.m01()) &&
                this.value.getLong(16) == Double.doubleToLongBits(value.m02()) &&
                this.value.getLong(24) == Double.doubleToLongBits(value.m03()) &&
                this.value.getLong(32) == Double.doubleToLongBits(value.m10()) &&
                this.value.getLong(40) == Double.doubleToLongBits(value.m11()) &&
                this.value.getLong(48) == Double.doubleToLongBits(value.m12()) &&
                this.value.getLong(56) == Double.doubleToLongBits(value.m13()) &&
                this.value.getLong(64) == Double.doubleToLongBits(value.m20()) &&
                this.value.getLong(72) == Double.doubleToLongBits(value.m21()) &&
                this.value.getLong(80) == Double.doubleToLongBits(value.m22()) &&
                this.value.getLong(88) == Double.doubleToLongBits(value.m23()) &&
                this.value.getLong(96) == Double.doubleToLongBits(value.m30()) &&
                this.value.getLong(104) == Double.doubleToLongBits(value.m31()) &&
                this.value.getLong(112) == Double.doubleToLongBits(value.m32()) &&
                this.value.getLong(120) == Double.doubleToLongBits(value.m33())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix2x3(Matrix3x2dc value, boolean transpose) {
        if (this.type != Type.MATRIX2x3) {
            return;
        }

        if (this.value.getLong(0) == Double.doubleToLongBits(value.m00()) &&
                this.value.getLong(4) == Double.doubleToLongBits(value.m01()) &&
                this.value.getLong(8) == Double.doubleToLongBits(value.m10()) &&
                this.value.getLong(16) == Double.doubleToLongBits(value.m11()) &&
                this.value.getLong(20) == Double.doubleToLongBits(value.m20()) &&
                this.value.getLong(24) == Double.doubleToLongBits(value.m21())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }

    @Override
    public void setMatrix3x2(Matrix3x2dc value, boolean transpose) {
        if (this.type != Type.MATRIX3x2) {
            return;
        }

        if (this.value.getLong(0) == Double.doubleToLongBits(value.m00()) &&
                this.value.getLong(4) == Double.doubleToLongBits(value.m01()) &&
                this.value.getLong(8) == Double.doubleToLongBits(value.m10()) &&
                this.value.getLong(16) == Double.doubleToLongBits(value.m11()) &&
                this.value.getLong(20) == Double.doubleToLongBits(value.m20()) &&
                this.value.getLong(24) == Double.doubleToLongBits(value.m21())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(!transpose);
    }

    @Override
    public void setMatrix3x4(Matrix4x3dc value, boolean transpose) {
        if (this.type != Type.MATRIX3x4) {
            return;
        }

        if (this.value.getLong(0) == Double.doubleToLongBits(value.m00()) &&
                this.value.getLong(4) == Double.doubleToLongBits(value.m01()) &&
                this.value.getLong(8) == Double.doubleToLongBits(value.m02()) &&
                this.value.getLong(12) == Double.doubleToLongBits(value.m10()) &&
                this.value.getLong(16) == Double.doubleToLongBits(value.m11()) &&
                this.value.getLong(20) == Double.doubleToLongBits(value.m12()) &&
                this.value.getLong(24) == Double.doubleToLongBits(value.m20()) &&
                this.value.getLong(28) == Double.doubleToLongBits(value.m21()) &&
                this.value.getLong(32) == Double.doubleToLongBits(value.m22()) &&
                this.value.getLong(36) == Double.doubleToLongBits(value.m30()) &&
                this.value.getLong(40) == Double.doubleToLongBits(value.m31()) &&
                this.value.getLong(44) == Double.doubleToLongBits(value.m32())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(!transpose);
    }

    @Override
    public void setMatrix4x3(Matrix4x3dc value, boolean transpose) {
        if (this.type != Type.MATRIX4x3) {
            return;
        }

        if (this.value.getLong(0) == Double.doubleToLongBits(value.m00()) &&
                this.value.getLong(4) == Double.doubleToLongBits(value.m01()) &&
                this.value.getLong(8) == Double.doubleToLongBits(value.m02()) &&
                this.value.getLong(12) == Double.doubleToLongBits(value.m10()) &&
                this.value.getLong(16) == Double.doubleToLongBits(value.m11()) &&
                this.value.getLong(20) == Double.doubleToLongBits(value.m12()) &&
                this.value.getLong(24) == Double.doubleToLongBits(value.m20()) &&
                this.value.getLong(28) == Double.doubleToLongBits(value.m21()) &&
                this.value.getLong(32) == Double.doubleToLongBits(value.m22()) &&
                this.value.getLong(36) == Double.doubleToLongBits(value.m30()) &&
                this.value.getLong(40) == Double.doubleToLongBits(value.m31()) &&
                this.value.getLong(44) == Double.doubleToLongBits(value.m32())) {
            return;
        }

        value.get(this.value);
        this.uploadMatrix(transpose);
    }
}
