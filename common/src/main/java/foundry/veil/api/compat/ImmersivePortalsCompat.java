package foundry.veil.api.compat;

import com.mojang.blaze3d.shaders.Program;
import foundry.veil.Veil;
import org.jetbrains.annotations.Nullable;

import java.util.ServiceLoader;

public interface ImmersivePortalsCompat {

    /**
     * Retrieves the compat instance. This will be <code>null</code> if Immersive Portals is not installed.
     */
    @Nullable
    ImmersivePortalsCompat INSTANCE = Veil.platform().isModLoaded("imm_ptl") ? ServiceLoader.load(ImmersivePortalsCompat.class).findFirst().orElse(null) : null;

    /**
     * @return Whether Immersive Portals is loaded
     */
    static boolean isLoaded() {
        return INSTANCE != null;
    }

    void init();

    String transform(Program.Type type, String shaderId, String inputCode);

    boolean shouldAddUniform(String shaderName);
}
