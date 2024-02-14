package foundry.veil.api.opencl;

/**
 * An OpenCL memory object that points to an OpenGL object. Objects should generally be freed after they are done being used.
 *
 * @author Ocelot
 * @see CLMemObject
 * @see CLKernel
 */
public interface CLGLMemObject extends CLMemObject {

    /**
     * Acquires the data referenced by this object from OpenGL to allow OpenCL to safely modify it.
     *
     * @throws CLException If any error occurs while trying to sync data
     */
    void acquireFromGL() throws CLException;

    /**
     * Releases the data referenced by this object from OpenCL to allow OpenGL to safely modify it again.
     *
     * @throws CLException If any error occurs while trying to sync data
     */
    void releaseToGL() throws CLException;
}
