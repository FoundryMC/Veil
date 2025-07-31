package foundry.veil.impl.client.render.shader.uniform;

import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import org.jetbrains.annotations.ApiStatus;
import org.joml.*;

@ApiStatus.Internal
public enum EmptyShaderUniformAccess implements ShaderUniformAccess {
    INSTANCE;

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void setFloat(float value) {
    }

    @Override
    public void setVector(float x, float y) {
    }

    @Override
    public void setVector(float x, float y, float z) {
    }

    @Override
    public void setVector(float x, float y, float z, float w) {
    }

    @Override
    public void setInt(int value) {
    }

    @Override
    public void setVectorI(int x, int y) {
    }

    @Override
    public void setVectorI(int x, int y, int z) {
    }

    @Override
    public void setVectorI(int x, int y, int z, int w) {
    }

    @Override
    public void setDouble(double value) {
    }

    @Override
    public void setVector64(double x, double y) {
    }

    @Override
    public void setVector64(double x, double y, double z) {
    }

    @Override
    public void setVector64(double x, double y, double z, double w) {
    }

    @Override
    public void setLong(long value) {
    }

    @Override
    public void setVectorI64(long x, long y) {
    }

    @Override
    public void setVectorI64(long x, long y, long z) {
    }

    @Override
    public void setVectorI64(long x, long y, long z, long w) {
    }

    @Override
    public void setFloats(float... values) {
    }

    @Override
    public void setVectors(Vector2fc... values) {
    }

    @Override
    public void setVectors(Vector3fc... values) {
    }

    @Override
    public void setVectors(Vector4fc... values) {
    }

    @Override
    public void setInts(int... values) {
    }

    @Override
    public void setIVectors(Vector2ic... values) {
    }

    @Override
    public void setIVectors(Vector3ic... values) {
    }

    @Override
    public void setIVectors(Vector4ic... values) {
    }

    @Override
    public void setDoubles(double... values) {
    }

    @Override
    public void set64Vectors(Vector2dc... values) {
    }

    @Override
    public void set64Vectors(Vector3dc... values) {
    }

    @Override
    public void set64Vectors(Vector4dc... values) {
    }

    @Override
    public void setLongs(long... values) {
    }

    @Override
    public void setHandle(long value) {
    }

    @Override
    public void setHandles(long... values) {
    }

    @Override
    public void setMatrix(Matrix2fc value, boolean transpose) {
    }

    @Override
    public void setMatrix(Matrix3fc value, boolean transpose) {
    }

    @Override
    public void setMatrix(Matrix4fc value, boolean transpose) {
    }

    @Override
    public void setMatrix2x3(Matrix3x2fc value, boolean transpose) {
    }

    @Override
    public void setMatrix3x2(Matrix3x2fc value, boolean transpose) {
    }

    @Override
    public void setMatrix3x4(Matrix4x3fc value, boolean transpose) {
    }

    @Override
    public void setMatrix4x3(Matrix4x3fc value, boolean transpose) {
    }

    @Override
    public void setMatrix(Matrix2dc value, boolean transpose) {
    }

    @Override
    public void setMatrix(Matrix3dc value, boolean transpose) {
    }

    @Override
    public void setMatrix(Matrix4dc value, boolean transpose) {
    }

    @Override
    public void setMatrix2x3(Matrix3x2dc value, boolean transpose) {
    }

    @Override
    public void setMatrix3x2(Matrix3x2dc value, boolean transpose) {
    }

    @Override
    public void setMatrix3x4(Matrix4x3dc value, boolean transpose) {
    }

    @Override
    public void setMatrix4x3(Matrix4x3dc value, boolean transpose) {
    }
}