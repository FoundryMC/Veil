package foundry.veil.opencl.event;

import foundry.veil.opencl.CLException;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Ocelot
 */
public interface CLEventDispatcher {

    void listen(long event, long eventType, @NotNull Runnable callback) throws CLException;

    void close() throws InterruptedException;
}
