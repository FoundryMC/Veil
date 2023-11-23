package foundry.veil.opencl;

import org.lwjgl.system.NativeResource;

/**
 * An OpenCL memory object. Objects should generally be freed after they are done being used.
 *
 * @author Ocelot
 * @see CLKernel
 */
public interface CLMemObject extends NativeResource {

    /**
     * @return The pointer to this object
     */
    long pointer();
}
