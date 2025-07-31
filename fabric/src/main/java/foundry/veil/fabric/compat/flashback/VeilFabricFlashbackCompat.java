package foundry.veil.fabric.compat.flashback;

import com.moulberry.flashback.editor.ui.ReplayUI;
import foundry.veil.api.compat.FlashbackCompat;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class VeilFabricFlashbackCompat implements FlashbackCompat {

    @Override
    public void backup(Matrix4f lastProjectionMatrix, Quaternionf lastViewQuaternion) {
        lastProjectionMatrix.set(ReplayUI.lastProjectionMatrix);
        lastViewQuaternion.set(ReplayUI.lastViewQuaternion);
    }

    @Override
    public void restore(Matrix4f lastProjectionMatrix, Quaternionf lastViewQuaternion) {
        ReplayUI.lastProjectionMatrix.set(lastProjectionMatrix);
        ReplayUI.lastViewQuaternion.set(lastViewQuaternion);
    }
}
