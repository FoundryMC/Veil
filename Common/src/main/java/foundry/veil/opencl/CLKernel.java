package foundry.veil.opencl;

import foundry.veil.Veil;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;

import java.nio.*;

import static org.lwjgl.opencl.CL10.*;

/**
 * <p>Manages the OpenCL kernel object. Buffers can be created with {@link #createBuffer(int, long)} and {@link #createBufferUnsafe(int, long)}</p>
 * <p><b>This does not need to be freed.</b></p>
 *
 * @author Ocelot
 */
public class CLKernel implements NativeResource {

    private final OpenCLEnvironment environment;
    private final long handle;
    private final LongSet pointers;

    CLKernel(OpenCLEnvironment environment, long handle) {
        this.environment = environment;
        this.handle = handle;
        this.pointers = new LongArraySet();
    }

    /**
     * Creates a new CL memory buffer. Any errors are consumed and printed to console.
     *
     * @param flags The creation flags
     * @param size  The size of the buffer in bytes
     * @return A data buffer that can be used with {@link #setPointers(int, long...)} or {@link #setPointers(int, CLMemObject...)} or <code>null</code> if an error occurred
     * @see CL10#clCreateBuffer(long, long, long, IntBuffer)
     */
    public CLBuffer createBufferUnsafe(int flags, long size) {
        try {
            return createBuffer(flags, size);
        } catch (OpenCLException e) {
            Veil.LOGGER.error("Failed to create CL buffer", e);
            return null;
        }
    }

    /**
     * Creates a new CL memory buffer.
     *
     * @param flags The creation flags
     * @param size  The size of the buffer in bytes
     * @return A data buffer that can be used with {@link #setPointers(int, long...)} or {@link #setPointers(int, CLMemObject...)}
     * @throws OpenCLException If there is any problem creating the buffer
     * @see CL10#clCreateBuffer(long, long, long, IntBuffer)
     */
    public CLBuffer createBuffer(int flags, long size) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer error_ret = stack.mallocInt(1);
            long pointer = clCreateBuffer(this.environment.getContext(), flags, size, error_ret);
            VeilOpenCL.checkCLError(error_ret.get(0));
            this.pointers.add(pointer);
            return new CLBuffer(this.environment, pointer);
        }
    }

    public void setBytes(int index, byte... value) throws OpenCLException {
        if (value.length == 0) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer arg_value = stack.bytes(value);
            VeilOpenCL.checkCLError(clSetKernelArg(this.handle, index, arg_value));
        }
    }

    public void setShorts(int index, short... value) throws OpenCLException {
        if (value.length == 0) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ShortBuffer arg_value = stack.shorts(value);
            VeilOpenCL.checkCLError(clSetKernelArg(this.handle, index, arg_value));
        }
    }

    public void setInts(int index, int... value) throws OpenCLException {
        if (value.length == 0) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer arg_value = stack.ints(value);
            VeilOpenCL.checkCLError(clSetKernelArg(this.handle, index, arg_value));
        }
    }

    public void setLongs(int index, long... value) throws OpenCLException {
        if (value.length == 0) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer arg_value = stack.longs(value);
            VeilOpenCL.checkCLError(clSetKernelArg(this.handle, index, arg_value));
        }
    }

    public void setFloats(int index, float... value) throws OpenCLException {
        if (value.length == 0) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer arg_value = stack.floats(value);
            VeilOpenCL.checkCLError(clSetKernelArg(this.handle, index, arg_value));
        }
    }

    public void setDoubles(int index, double... value) throws OpenCLException {
        if (value.length == 0) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer arg_value = stack.doubles(value);
            VeilOpenCL.checkCLError(clSetKernelArg(this.handle, index, arg_value));
        }
    }

    public void setPointers(int index, long... value) throws OpenCLException {
        if (value.length == 0) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer arg_value = stack.pointers(value);
            VeilOpenCL.checkCLError(clSetKernelArg(this.handle, index, arg_value));
        }
    }

    public void setPointers(int index, CLMemObject... value) throws OpenCLException {
        if (value.length == 0) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer arg_value = stack.mallocPointer(value.length);
            for (CLMemObject object : value) {
                arg_value.put(object.pointer());
            }
            arg_value.rewind();
            VeilOpenCL.checkCLError(clSetKernelArg(this.handle, index, arg_value));
        }
    }

    /**
     * @return The pointer to the kernel object
     */
    public long getHandle() {
        return this.handle;
    }

    @Override
    public void free() {
        clReleaseKernel(this.handle);
        this.pointers.forEach(CL10::clReleaseMemObject);
        this.pointers.clear();
    }
}
