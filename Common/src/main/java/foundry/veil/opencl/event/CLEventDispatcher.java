package foundry.veil.opencl.event;

import foundry.veil.opencl.CLException;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opencl.CL10;

/**
 * Provides low-level access to event subscriptions. This allows a callback to be fired when OpenCL fires an event.
 *
 * @author Ocelot
 */
public interface CLEventDispatcher {

    /**
     * Listens to the specific event and event status.
     *
     * @param event       The event to listen to. This is a pointer to an event provided by an OpenCL function.
     * @param eventStatus The event status to listen for. One of {@link CL10#CL_COMPLETE}, {@link CL10#CL_RUNNING}, {@link CL10#CL_SUBMITTED}, or {@link CL10#CL_QUEUED}
     * @param callback    The callback to fire when the event fires
     * @throws CLException If the event is invalid
     */
    void listen(long event, long eventStatus, @NotNull Runnable callback) throws CLException;
}
