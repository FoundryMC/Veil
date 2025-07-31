package foundry.veil.impl.client.render.perspective;

import foundry.veil.api.compat.FlashbackCompat;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class FlashbackAccess {

    public static void backup(Matrix4f lastProjectionMatrix, Quaternionf lastViewQuaternion) {
        if (FlashbackCompat.isLoaded()) {
            FlashbackCompat.INSTANCE.backup(lastProjectionMatrix, lastViewQuaternion);
        }
    }

    public static void restore(Matrix4f lastProjectionMatrix, Quaternionf lastViewQuaternion) {
        if (FlashbackCompat.isLoaded()) {
            FlashbackCompat.INSTANCE.restore(lastProjectionMatrix, lastViewQuaternion);
        }
    }

}
