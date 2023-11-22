package foundry.veil.opencl;

/**
 * An OpenCL memory object.
 *
 * @author Ocelot
 * @see CLKernel
 */
public interface CLMemObject {

    /**
     * @return The pointer to this object
     */
    long pointer();
}
