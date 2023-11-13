package foundry.veil.mixin.client;//package foundry.veil.quasar.mixin.client;
//
//import foundry.veil.quasar.util.CameraExpansion;
//import net.minecraft.client.Camera;
//import net.minecraft.world.phys.AABB;
//import net.minecraft.world.phys.Vec3;
//import org.joml.Vector3f;
//import org.spongepowered.asm.mixin.Mixin;
//
//@Mixin(Camera.class)
//public class CameraMixin implements CameraExpansion {
//    @Override
//    public boolean isBoundingBoxInFrustum(AABB aabbIn) {
//        Camera camera = (Camera) (Object) this;
//        Camera.NearPlane near = camera.getNearPlane();
//        Vec3 vec3d = camera.getPosition();
//        Vector3f vec3d1 = camera.getLookVector();
//        Vector3f vec3d2 = camera.getUpVector();
//        Vector3f vec3d3 = vec3d1.copy();
//        vec3d3.cross(vec3d2);
//        Vector3f vec3d4 = vec3d2.copy();
//        vec3d4.cross(vec3d3);
//        double d0 = vec3d1.x() * vec3d.x() + vec3d1.y() * vec3d.y() + vec3d1.z() * vec3d.z();
//        double d1 = vec3d1.x() * aabbIn.minX + vec3d1.y() * aabbIn.minY + vec3d1.z() * aabbIn.minZ - d0;
//        double d2 = vec3d1.x() * aabbIn.maxX + vec3d1.y() * aabbIn.maxY + vec3d1.z() * aabbIn.maxZ - d0;
//        double d3 = vec3d4.x() * aabbIn.minX + vec3d4.y() * aabbIn.minY + vec3d4.z() * aabbIn.minZ;
//        double d4 = vec3d4.x() * aabbIn.maxX + vec3d4.y() * aabbIn.maxY + vec3d4.z() * aabbIn.maxZ;
//        double d5 = vec3d3.x() * aabbIn.minX + vec3d3.y() * aabbIn.minY + vec3d3.z() * aabbIn.minZ;
//        double d6 = vec3d3.x() * aabbIn.maxX + vec3d3.y() * aabbIn.maxY + vec3d3.z() * aabbIn.maxZ;
//        if (d1 * d2 < 0.0D && d3 * d4 < 0.0D && d5 * d6 < 0.0D) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//}
