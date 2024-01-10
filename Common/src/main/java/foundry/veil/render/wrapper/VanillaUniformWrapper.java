package foundry.veil.render.wrapper;

import com.mojang.blaze3d.shaders.Uniform;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL41C.*;

@ApiStatus.Internal
public class VanillaUniformWrapper extends Uniform {

    private final int programId;
    private int location;

    public VanillaUniformWrapper(int shaderId, String name) {
        super(name, UT_INT1, 0, null);
        this.close(); // Free constructor allocated resources
        this.programId = shaderId;
        this.location = -1;
    }

    @Override
    public void setLocation(int location) {
        this.location = location;
    }

    @Override
    public void set(int index, float value) {
        throw new UnsupportedOperationException("Use absolute set");
    }

    @Override
    public void set(float value) {
        if (this.location != -1) {
            glProgramUniform1f(this.programId, this.location, value);
        }
    }

    @Override
    public void set(float x, float y) {
        if (this.location != -1) {
            glProgramUniform2f(this.programId, this.location, x, y);
        }
    }

    @Override
    public void set(float x, float y, float z) {
        if (this.location != -1) {
            glProgramUniform3f(this.programId, this.location, x, y, z);
        }
    }

    @Override
    public void set(float x, float y, float z, float w) {
        if (this.location != -1) {
            glProgramUniform4f(this.programId, this.location, x, y, z, w);
        }
    }

    @Override
    public void set(@NotNull Vector3f value) {
        this.set(value.x, value.y, value.z);
    }

    @Override
    public void set(@NotNull Vector4f value) {
        this.set(value.x, value.y, value.z, value.w);
    }

    @Override
    public void setSafe(float x, float y, float z, float w) {
        this.set(x, y, z, w);
    }

    @Override
    public void set(int value) {
        if (this.location != -1) {
            glProgramUniform1i(this.programId, this.location, value);
        }
    }

    @Override
    public void set(int x, int y) {
        if (this.location != -1) {
            glProgramUniform2i(this.programId, this.location, x, y);
        }
    }

    @Override
    public void set(int x, int y, int z) {
        if (this.location != -1) {
            glProgramUniform3i(this.programId, this.location, x, y, z);
        }
    }

    @Override
    public void set(int x, int y, int z, int w) {
        if (this.location != -1) {
            glProgramUniform4i(this.programId, this.location, x, y, z, w);
        }
    }

    @Override
    public void setSafe(int x, int y, int z, int w) {
        this.set(x, y, z, w);
    }

    @Override
    public void set(float[] values) {
        switch (values.length) {
            case 1 -> this.set(values[0]);
            case 2 -> this.set(values[0], values[1]);
            case 3 -> this.set(values[0], values[1], values[2]);
            case 4 -> this.set(values[0], values[1], values[2], values[3]);
            default -> throw new UnsupportedOperationException("Invalid value array: " + Arrays.toString(values));
        }
    }

    @Override
    public void setMat2x2(float $$0, float $$1, float $$2, float $$3) {
        throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
    }

    @Override
    public void setMat2x3(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5) {
        throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
    }

    @Override
    public void setMat2x4(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7) {
        throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
    }

    @Override
    public void setMat3x2(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5) {
        throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
    }

    @Override
    public void setMat3x3(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7,
                          float $$8) {
        throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
    }

    @Override
    public void setMat3x4(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7,
                          float $$8, float $$9, float $$10, float $$11) {
        throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
    }

    @Override
    public void setMat4x2(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7) {
        throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
    }

    @Override
    public void setMat4x3(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7,
                          float $$8, float $$9, float $$10, float $$11) {
        throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
    }

    @Override
    public void setMat4x4(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7,
                          float $$8, float $$9, float $$10, float $$11, float $$12, float $$13, float $$14,
                          float $$15) {
        throw new UnsupportedOperationException("Use #set(Matrix4fc) or #set(Matrix3fc) instead");
    }

    @Override
    public void set(@NotNull Matrix3f value) {
        if (this.location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3 * 3);
            value.get(buffer);
            glProgramUniformMatrix2fv(this.programId, this.location, false, buffer);
        }
    }

    @Override
    public void set(@NotNull Matrix4f value) {
        if (this.location == -1) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4 * 4);
            value.get(buffer);
            glProgramUniformMatrix2fv(this.programId, this.location, false, buffer);
        }
    }

    @Override
    public void upload() {
    }

    @Override
    public int getLocation() {
        return this.location;
    }
}
