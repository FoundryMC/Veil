package foundry.veil.opencl.event;

import com.mojang.logging.LogUtils;
import foundry.veil.opencl.OpenCLException;
import foundry.veil.opencl.VeilOpenCL;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

import java.nio.IntBuffer;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.opencl.CL10.CL_EVENT_COMMAND_EXECUTION_STATUS;
import static org.lwjgl.opencl.CL10.clGetEventInfo;

/**
 * Uses a custom thread to request event status for all events.
 */
@ApiStatus.Internal
public class CLLegacyEventDispatcher implements CLEventDispatcher {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);

    private final Queue<EventListener> eventListeners;
    private final Object eventNotifier;
    private final Thread listenerThread;
    private volatile boolean stopped;

    public CLLegacyEventDispatcher() {
        this.eventListeners = new ConcurrentLinkedQueue<>();
        this.eventNotifier = new Object();

        this.listenerThread = new Thread(this::process, "CL Event Listener #" + WORKER_COUNT.getAndIncrement());
        this.listenerThread.start();
    }

    private void process() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer status = stack.mallocInt(1);
            while (true) {
                EventListener event = this.eventListeners.poll();
                if (event == null) {
                    if (this.stopped) {
                        return;
                    }

                    try {
                        synchronized (this.eventNotifier) {
                            this.eventNotifier.wait();
                        }
                    } catch (InterruptedException e) {
                        LOGGER.warn("Error while waiting for events", e);
                    }
                    continue;
                }

                try {
                    VeilOpenCL.checkCLError(clGetEventInfo(event.event, CL_EVENT_COMMAND_EXECUTION_STATUS, status, null));

                    if (status.get(0) <= event.eventType) {
                        event.callback.run();
                        continue;
                    }

                    this.eventListeners.add(event);
                } catch (OpenCLException e) {
                    LOGGER.error("Error while querying event", e);
                }
            }
        }
    }

    @Override
    public void listen(long event, long eventType, @NotNull Runnable callback) {
        Objects.requireNonNull(callback, "callback");
        this.eventListeners.add(new EventListener(event, eventType, callback));
        synchronized (this.eventNotifier) {
            this.eventNotifier.notifyAll();
        }
    }

    @Override
    public void close() throws InterruptedException {
        this.stopped = true;
        synchronized (this.eventNotifier) {
            this.eventNotifier.notifyAll();
        }

        // Wait at most 4 seconds
        this.listenerThread.join(4000);
    }

    private record EventListener(long event, long eventType, Runnable callback) {
    }
}
