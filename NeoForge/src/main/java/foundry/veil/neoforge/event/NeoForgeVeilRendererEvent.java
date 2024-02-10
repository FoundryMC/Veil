package foundry.veil.neoforge.event;

import foundry.veil.api.client.render.VeilRenderer;
import net.neoforged.bus.api.Event;

/**
 * Fired when Veil has finished initialization and the renderer is safe to use.
 *
 * @author Ocelot
 */
public class NeoForgeVeilRendererEvent extends Event {

    private final VeilRenderer renderer;

    public NeoForgeVeilRendererEvent(VeilRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * @return The renderer instance
     */
    public VeilRenderer getRenderer() {
        return this.renderer;
    }
}
