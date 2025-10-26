package foundry.veil.api.compat;

import foundry.veil.Veil;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ServiceLoader;

/**
 * Veil flashback compat implementation.
 *
 * @author SpacePotato
 */
public interface FlashbackCompat {

    FlashbackCompat INSTANCE = Veil.platform().isModLoaded("flashback") ? ServiceLoader.load(FlashbackCompat.class).findFirst().orElse(null) : null;

    /**
     * @return Whether flashback is loaded
     */
    static boolean isLoaded() {
        return INSTANCE != null;
    }

    /**
     * Stores the values of the respective ReplayUI fields
     *
     * @param lastProjectionMatrix The backup matrix used to store ReplayUI's projection matrix
     * @param lastViewQuaternion   The backup quaternion used to store ReplayUI's camera rotation
     */
    void backup(Matrix4f lastProjectionMatrix, Quaternionf lastViewQuaternion);

    /**
     * Sets the values of the respective ReplayUI fields
     *
     * @param lastProjectionMatrix The matrix that ReplayUI's projection matrix should be set to
     * @param lastViewQuaternion   The quaternion that ReplayUI's camera rotation should be set to
     */
    void restore(Matrix4f lastProjectionMatrix, Quaternionf lastViewQuaternion);
}
