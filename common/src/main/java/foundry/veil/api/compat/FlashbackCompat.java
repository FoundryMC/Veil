package foundry.veil.api.compat;

import foundry.veil.Veil;

/**
 * Veil flashback compat implementation.
 *
 * @author SpacePotato
 */
public interface FlashbackCompat {

    boolean IS_LOADED = Veil.platform().isModLoaded("flashback");

    /**
     * @return Whether flashback is loaded
     */
    static boolean isLoaded() {
        return IS_LOADED;
    }

}
