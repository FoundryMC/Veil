package foundry.veil.editor;

import org.lwjgl.system.NativeResource;

/**
 * A basic panel that can be toggled in the editor view.
 *
 * @author Ocelot
 */
public interface Editor extends NativeResource {

    /**
     * Renders elements into the menu bar. Only called if {@link #isMenuBarEnabled()} is <code>true</code>.
     */
    default void renderMenuBar() {
    }

    /**
     * Renders this editor to the screen.
     */
    void render();

    /**
     * @return The visible display name of this editor
     */
    String getDisplayName();

    /**
     * @return Whether this editor should be selectable.
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * @return Whether this editor should draw into the menu bar
     */
    default boolean isMenuBarEnabled() {
        return false;
    }

    /**
     * Called when this editor is first opened.
     */
    default void onShow() {
    }

    /**
     * Called when this editor is no longer open.
     */
    default void onHide() {
    }

    /**
     * Frees any resources allocated by this editor before being destroyed.
     */
    @Override
    default void free() {
    }
}
