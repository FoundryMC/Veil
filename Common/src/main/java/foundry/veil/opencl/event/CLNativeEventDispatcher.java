package foundry.veil.opencl.event;

import foundry.veil.opencl.CLException;
import foundry.veil.opencl.VeilOpenCL;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import java.util.Objects;

import static org.lwjgl.opencl.CL10.CL_COMPLETE;
import static org.lwjgl.opencl.CL11.clSetEventCallback;

/**
 * Uses the native OpenCL event callback system to listen to events. Only supported on OpenCL 1.1 and above.
 */
@ApiStatus.Internal
public class CLNativeEventDispatcher implements CLEventDispatcher {

    @Override
    public void listen(long event, long eventType, @NotNull Runnable callback) throws CLException {
        Objects.requireNonNull(callback, "callback");
        VeilOpenCL.checkCLError(clSetEventCallback(event, CL_COMPLETE, (e, event_command_exec_status, user_data) -> {
            if (event_command_exec_status == CL_COMPLETE) {
                callback.run();
            }
        }, MemoryUtil.NULL));
    }

    @Override
    public void close() {
    }
}
