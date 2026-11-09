package foundry.veil.api.client.necromancer.animation.keyframe;

import foundry.veil.api.client.util.Easing;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public record Keyframe(float time, Easing easing, KeyframeTransform transform) {
    record KeyframeTransform(float px, float py, float pz, float sx, float sy, float sz, float qx, float qy, float qz,
                             float qw) {
        public KeyframeTransform(float px, float py, float pz, float sx, float sy, float sz, Quaternionfc quaternion) {
            this(px, py, pz, sx, sy, sz, quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w());
        }

        public KeyframeTransform(Vector3fc pos, Vector3fc size, Quaternionfc quaternion) {
            this(pos.x(), pos.y(), pos.z(), size.x(), size.y(), size.z(), quaternion);
        }
    }
}
