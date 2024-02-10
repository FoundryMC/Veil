package foundry.veil.neoforge.event;

import foundry.veil.api.event.VeilRegisterFixedBuffersEvent;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.platform.services.VeilEventPlatform;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * <p>Registers custom fixed render type buffers.</p>
 * <p>Use {@link RenderLevelStageEvent} or {@link VeilEventPlatform#onVeilRenderTypeStageRender(VeilRenderLevelStageEvent)}  to listen to level stage render events on Forge.</p>
 *
 * @author Ocelot
 * @see VeilRegisterFixedBuffersEvent
 */
public class NeoForgeVeilRegisterFixedBuffersEvent extends Event {

    private final BiConsumer<RenderLevelStageEvent.Stage, RenderType> registry;

    public NeoForgeVeilRegisterFixedBuffersEvent(BiConsumer<RenderLevelStageEvent.Stage, RenderType> registry) {
        this.registry = registry;
    }

    /**
     * Registers a fixed render type.
     *
     * @param stage      The stage the buffer should be finished after or <code>null</code> to do it manually
     * @param renderType The render type to finish
     */
    public void register(@Nullable RenderLevelStageEvent.Stage stage, RenderType renderType) {
        this.registry.accept(stage, renderType);
    }
}
