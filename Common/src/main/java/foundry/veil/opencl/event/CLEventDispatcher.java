package foundry.veil.opencl.event;

import foundry.veil.opencl.OpenCLException;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Ocelot
 */
public interface CLEventDispatcher {

    void listen(long event, long eventType, @NotNull Runnable callback) throws OpenCLException;

    void close() throws InterruptedException;
}
