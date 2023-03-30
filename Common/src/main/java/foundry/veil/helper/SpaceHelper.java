package foundry.veil.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

public class SpaceHelper {
    public static Vector3f getNormalizedDeviceCoords(float x, float y, float z, float width, float height) {
        float rx = (2.0f * x) / width - 1.0f;
        float ry = 1.0f - (2.0f * y) / height;
        return new Vector3f(rx, ry, z);
    }

    public static Vector4f getClipCoords(Vector3f ndc) {
        return new Vector4f(ndc.x(), ndc.y(), -1.0f, 1.0f);
    }

    public static Vector4f getEyeCoords(Vector4f clipCoords) {
        Matrix4f invProjMatrix = RenderSystem.getProjectionMatrix().copy();
        invProjMatrix.invert();
        clipCoords.transform(invProjMatrix);
        return new Vector4f(clipCoords.x(), clipCoords.y(), -1.0f, 0.0f);
    }

    public static Vector3f getWorldCoords(Vector4f eyeCoords) {
        Matrix4f invViewMatrix = RenderSystem.getModelViewMatrix().copy();
        invViewMatrix.invert();
        eyeCoords.transform(invViewMatrix);
        return new Vector3f(eyeCoords.x(), eyeCoords.y(), eyeCoords.z());
    }

    public static Vector3f worldToScreenCoords(Vector3f worldCoords, float width, float height) {
        Vector3f ndc = getNormalizedDeviceCoords(worldCoords.x(), worldCoords.y(), worldCoords.z(), width, height);
        Vector4f clipCoords = getClipCoords(ndc);
        Vector4f eyeCoords = getEyeCoords(clipCoords);
        Vector3f screenCoords = getWorldCoords(eyeCoords);
        return new Vector3f(screenCoords.x(), screenCoords.y(), screenCoords.z());
    }

    public Vector4f convertMatrixToVector4f(Matrix4f matrix) {
        return new Vector4f(matrix.m03, matrix.m13, matrix.m23, matrix.m33);
    }
}
