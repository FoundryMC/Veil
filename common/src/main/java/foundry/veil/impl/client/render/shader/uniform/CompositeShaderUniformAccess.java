package foundry.veil.impl.client.render.shader.uniform;

import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import org.jetbrains.annotations.ApiStatus;
import org.joml.*;

@ApiStatus.Internal
public record CompositeShaderUniformAccess(ShaderUniformAccess... accesses) implements ShaderUniformAccess {

    @Override
    public boolean isValid() {
        for (ShaderUniformAccess access : this.accesses) {
            if (access.isValid()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setFloat(float value) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setFloat(value);
        }
    }

    @Override
    public void setVector(float x, float y) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVector(x, y);
        }
    }

    @Override
    public void setVector(float x, float y, float z) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVector(x, y, z);
        }
    }

    @Override
    public void setVector(float x, float y, float z, float w) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVector(x, y, z, w);
        }
    }

    @Override
    public void setInt(int value) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setInt(value);
        }
    }

    @Override
    public void setVectorI(int x, int y) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVectorI(x, y);
        }
    }

    @Override
    public void setVectorI(int x, int y, int z) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVectorI(x, y, z);
        }
    }

    @Override
    public void setVectorI(int x, int y, int z, int w) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVectorI(x, y, z, w);
        }
    }

    @Override
    public void setDouble(double value) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setDouble(value);
        }
    }

    @Override
    public void setVector64(double x, double y) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVector64(x, y);
        }
    }

    @Override
    public void setVector64(double x, double y, double z) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVector64(x, y, z);
        }
    }

    @Override
    public void setVector64(double x, double y, double z, double w) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVector64(x, y, z, w);
        }
    }

    @Override
    public void setLong(long value) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setLong(value);
        }
    }

    @Override
    public void setVectorI64(long x, long y) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVectorI64(x, y);
        }
    }

    @Override
    public void setVectorI64(long x, long y, long z) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVectorI64(x, y, z);
        }
    }

    @Override
    public void setVectorI64(long x, long y, long z, long w) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVectorI64(x, y, z, w);
        }
    }

    @Override
    public void setFloats(float... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setFloats(values);
        }
    }

    @Override
    public void setVectors(Vector2fc... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVectors(values);
        }
    }

    @Override
    public void setVectors(Vector3fc... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVectors(values);
        }
    }

    @Override
    public void setVectors(Vector4fc... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setVectors(values);
        }
    }

    @Override
    public void setInts(int... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setInts(values);
        }
    }

    @Override
    public void setIVectors(Vector2ic... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setIVectors(values);
        }
    }

    @Override
    public void setIVectors(Vector3ic... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setIVectors(values);
        }
    }

    @Override
    public void setIVectors(Vector4ic... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setIVectors(values);
        }
    }

    @Override
    public void setDoubles(double... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setDoubles(values);
        }
    }

    @Override
    public void set64Vectors(Vector2dc... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.set64Vectors(values);
        }
    }

    @Override
    public void set64Vectors(Vector3dc... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.set64Vectors(values);
        }
    }

    @Override
    public void set64Vectors(Vector4dc... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.set64Vectors(values);
        }
    }

    @Override
    public void setLongs(long... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setLongs(values);
        }
    }

    @Override
    public void setHandle(long value) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setHandle(value);
        }
    }

    @Override
    public void setHandles(long... values) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setHandles(values);
        }
    }

    @Override
    public void setMatrix(Matrix2fc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix(value, transpose);
        }
    }

    @Override
    public void setMatrix(Matrix3fc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix(value, transpose);
        }
    }

    @Override
    public void setMatrix(Matrix4fc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix(value, transpose);
        }
    }

    @Override
    public void setMatrix2x3(Matrix3x2fc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix2x3(value, transpose);
        }
    }

    @Override
    public void setMatrix3x2(Matrix3x2fc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix3x2(value, transpose);
        }
    }

    @Override
    public void setMatrix3x4(Matrix4x3fc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix3x4(value, transpose);
        }
    }

    @Override
    public void setMatrix4x3(Matrix4x3fc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix4x3(value, transpose);
        }
    }

    @Override
    public void setMatrix(Matrix2dc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix(value, transpose);
        }
    }

    @Override
    public void setMatrix(Matrix3dc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix(value, transpose);
        }
    }

    @Override
    public void setMatrix(Matrix4dc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix(value, transpose);
        }
    }

    @Override
    public void setMatrix2x3(Matrix3x2dc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix2x3(value, transpose);
        }
    }

    @Override
    public void setMatrix3x2(Matrix3x2dc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix3x2(value, transpose);
        }
    }

    @Override
    public void setMatrix3x4(Matrix4x3dc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix3x4(value, transpose);
        }
    }

    @Override
    public void setMatrix4x3(Matrix4x3dc value, boolean transpose) {
        for (ShaderUniformAccess access : this.accesses) {
            access.setMatrix4x3(value, transpose);
        }
    }
}
