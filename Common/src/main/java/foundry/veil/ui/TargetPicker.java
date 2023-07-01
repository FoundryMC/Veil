package foundry.veil.ui;

import foundry.veil.helper.SpaceHelper;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class TargetPicker {

//    public static Vec3 getRay(Matrix4f projectionMatrix, Matrix4f viewMatrix, float normalizedMouseX, float normalizedMouseY) {
//        Vector4f clipCoords = new Vector4f(normalizedMouseX, -normalizedMouseY, -1.0F, 1.0F);
//        Vector4f eyeSpace = toEyeCoords(projectionMatrix, clipCoords);
//        return toWorldCoords(viewMatrix, eyeSpace);
//    }
//
//    private static Vector4f toEyeCoords(Matrix4f projectionMatrix, Vector4f clipCoords) {
//        Matrix4f inverse = SpaceHelper.invertProjection(projectionMatrix);
//        Vector4f result = new Vector4f(clipCoords.x(), clipCoords.y(), clipCoords.z(), clipCoords.w());
//        result.mul(inverse);
//        result.set(result.x(), result.y(), -1.0F, 0.0F);
//        return result;
//    }
//
//    private static Vec3 toWorldCoords(Matrix4f viewMatrix, Vector4f eyeCoords) {
//        Matrix4f inverse = SpaceHelper.invertGeneric(viewMatrix);
//        Vector4f result = new Vector4f(eyeCoords.x(), eyeCoords.y(), eyeCoords.z(), eyeCoords.w());
//        result.mul(inverse);
//        return new Vec3(result.x(), result.y(), result.z()).normalize();
//    }
}
