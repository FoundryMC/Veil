package foundry.veil.api.event;

import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Fired to register additional fixed render types.</p>
 * <p>Fixed buffers are batched together and are not drawn until after the specified stage is drawn. This should be used in most cases to defer a specific render type to a specific time.</p>
 *
 * @author Ocelot
 */
@FunctionalInterface
public interface VeilRegisterFixedBuffersEvent {

    /**
     * Registers fixed render types. The stage determines when the buffer should be finished and the render type is the layer to finish.
     *
     * @param registry The registry to add render types to
     */
    void onRegisterFixedBuffers(Registry registry);

    /**
     * Registers additional fixed render buffers.
     *
     * @author Ocelot
     */
    @FunctionalInterface
    interface Registry {

        /**
         * Registers the specified render type as a fixed buffer. That means all render calls using it will be batched until the specified stage is drawn.
         *
         * @param stage      The stage to draw the buffer after or <code>null</code> to do it manually
         * @param renderType The render type to register
         */
        void registerFixedBuffer(@Nullable VeilRenderLevelStageEvent.Stage stage, RenderType renderType);
    }
}
