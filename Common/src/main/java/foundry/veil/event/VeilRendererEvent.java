package foundry.veil.event;

import foundry.veil.render.pipeline.VeilRenderer;

/**
 * Fired when Veil has finished initialization and the renderer is safe to use.
 *
 * @author Ocelot
 */
@FunctionalInterface
public interface VeilRendererEvent {

    /**
     * Called when the Veil renderer is now available.
     *
     * @param renderer The renderer instance
     */
    void onVeilRendererAvailable(VeilRenderer renderer);
}
