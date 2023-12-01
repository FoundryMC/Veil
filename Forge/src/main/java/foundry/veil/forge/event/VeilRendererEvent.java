package foundry.veil.forge.event;

import foundry.veil.render.pipeline.VeilRenderer;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when Veil has finished initialization and the renderer is safe to use.
 *
 * @author Ocelot
 */
public class VeilRendererEvent extends Event {

    private final VeilRenderer renderer;

    public VeilRendererEvent(VeilRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * @return The renderer instance
     */
    public VeilRenderer getRenderer() {
        return this.renderer;
    }
}
