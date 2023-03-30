package foundry.veil.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

public class SpaceHelper {
    public static Vector3f getNormalizedDeviceCoordsFromViewport(float x, float y, float z, float width, float height) {
        float rx = (2.0f * x) / width - 1.0f;
        float ry = 1.0f - (2.0f * y) / height;
        return new Vector3f(rx, ry, z);
    }

    public static Vector3f getViewPortCoordFromNDC(Vector3f ndc) {
        return new Vector3f((ndc.x() + 1.0f) / 2.0f, (1.0f - ndc.y()) / 2.0f, ndc.z());
    }

    public static Vector4f getClipCoordsFromNDC(Vector3f ndc) {
        return new Vector4f(ndc.x(), ndc.y(), -1.0f, 1.0f);
    }

    public static Vector4f getNDCFromClip(Vector4f clipCoords) {
        clipCoords.perspectiveDivide();
        return clipCoords;

    }

    public static Vector4f getClipFromEye(Vector4f eyeCoords) {
        // transform by projection matrix
        Matrix4f projMatrix = RenderSystem.getProjectionMatrix().copy();
        eyeCoords.transform(projMatrix);
        return eyeCoords;
    }

    public static Vector4f getEyeFromWorld(Vector4f worldCoords) {
        // transform by view matrix
        Matrix4f viewMatrix = RenderSystem.getModelViewMatrix().copy();
        worldCoords.transform(viewMatrix);
        return worldCoords;
    }

    public static Vector4f getEyeCoordsFromClip(Vector4f clipCoords) {
        Matrix4f invProjMatrix = RenderSystem.getProjectionMatrix().copy();
        invProjMatrix.invert();
        clipCoords.transform(invProjMatrix);
        return new Vector4f(clipCoords.x(), clipCoords.y(), -1.0f, 0.0f);
    }

    public static Vector3f getWorldCoordsFromEye(Vector4f eyeCoords) {
        Matrix4f invViewMatrix = RenderSystem.getModelViewMatrix().copy();
        invViewMatrix.invert();
        eyeCoords.transform(invViewMatrix);
        return new Vector3f(eyeCoords.x(), eyeCoords.y(), eyeCoords.z());
    }

    public static Vector3f screenToWorldCoords(Vector3f worldCoords, float width, float height) {
        Vector3f ndc = getNormalizedDeviceCoordsFromViewport(worldCoords.x(), worldCoords.y(), worldCoords.z(), width, height);
        Vector4f clipCoords = getClipCoordsFromNDC(ndc);
        Vector4f eyeCoords = getEyeCoordsFromClip(clipCoords);
        Vector3f screenCoords = getWorldCoordsFromEye(eyeCoords);
        return new Vector3f(screenCoords.x(), screenCoords.y(), screenCoords.z());
    }

    public static Vector3f worldToScreenCoords(Vector3f worldCoords){
        Vector4f worldCoords4f = new Vector4f(worldCoords.x(), worldCoords.y(), worldCoords.z(), 1.0f);
        Vector4f eyeCoords = getEyeFromWorld(worldCoords4f);
        Vector4f clipCoords = getClipFromEye(eyeCoords);
        Vector4f ndc = getNDCFromClip(clipCoords);
        Vector3f screenCoords = getViewPortCoordFromNDC(new Vector3f(ndc.x(), ndc.y(), ndc.z()));
        return new Vector3f(screenCoords.x(), screenCoords.y(), screenCoords.z());
    }

    public Vector4f convertMatrixToVector4f(Matrix4f matrix) {
        return new Vector4f(matrix.m03, matrix.m13, matrix.m23, matrix.m33);
    }
}
