package foundry.veil.imgui;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

/**
 * Manages the internal ImGui state.
 *
 * @author Ocelot
 */
@ApiStatus.Internal
public interface VeilImGui extends NativeResource {

    void begin();

    void end();

    void windowFocusCallback(long window, boolean focused);

    void cursorEnterCallback(long window, boolean entered);

    void mouseButtonCallback(long window, int button, int action, int mods);

    void scrollCallback(long window, double xOffset, double yOffset);

    void keyCallback(long window, int key, int scancode, int action, int mods);

    void charCallback(long window, int codepoint);

}
