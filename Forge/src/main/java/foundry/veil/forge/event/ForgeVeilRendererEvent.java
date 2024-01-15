package foundry.veil.forge.event;

import foundry.veil.api.client.render.VeilRenderer;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when Veil has finished initialization and the renderer is safe to use.
 *
 * @author Ocelot
 */
public class ForgeVeilRendererEvent extends Event {

    private final VeilRenderer renderer;

    public ForgeVeilRendererEvent(VeilRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * @return The renderer instance
     */
    public VeilRenderer getRenderer() {
        return this.renderer;
    }
}
