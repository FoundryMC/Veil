package foundry.veil.opencl;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.system.MemoryStack;

import java.nio.*;

import static org.lwjgl.opencl.CL10.*;

/**
 * <p>A generic data buffer that can be referenced uploaded to/from OpenCL.</p>
 * <p>When using the buffer methods, the data should be provided as follows:</p>
 * <pre>
 *         // Using MemoryStack is fast, but limits the amount of data that can be allocated
 *         try (MemoryStack stack = MemoryStack.stackPush()) {
 *             // position should be 0 so the data can all be read
 *             ByteBuffer data = stack.malloc(2);
 *             data.put(0, (byte) 4);
 *             data.put(1, (byte) 7);
 *
 *             // rewind can be used if the position is mutated
 *             data.rewind();
 *
 *             // You get a buffer from a kernel
 *             CLBuffer buffer = ...;
 *
 *             buffer.write(data);
 *             // buffer.pointer() is still going to be 0 after this call
 *         }
 * </pre>
 *
 * @author Ocelot
 */
public class CLBuffer implements CLMemObject {

    private final OpenCLEnvironment environment;
    private final long pointer;

    CLBuffer(OpenCLEnvironment environment, long pointer) {
        this.environment = environment;
        this.pointer = pointer;
    }

    /**
     * Writes the specified data into this buffer.
     *
     * @param data The buffer with data to write
     * @throws OpenCLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, ByteBuffer, PointerBuffer, PointerBuffer)
     */
    public void write(ByteBuffer data) throws OpenCLException {
        VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, true, 0, data, null, null));
    }

    /**
     * Writes the specified data into this buffer.
     *
     * @param data The data to write
     * @throws OpenCLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, ShortBuffer, PointerBuffer, PointerBuffer)
     */
    public void write(ShortBuffer data) throws OpenCLException {
        VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, true, 0, data, null, null));
    }

    /**
     * Writes the specified data into this buffer.
     *
     * @param data The data to write
     * @throws OpenCLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, IntBuffer, PointerBuffer, PointerBuffer)
     */
    public void write(IntBuffer data) throws OpenCLException {
        VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, true, 0, data, null, null));
    }

    /**
     * Writes the specified data into this buffer.
     *
     * @param data The data to write
     * @throws OpenCLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, FloatBuffer, PointerBuffer, PointerBuffer)
     */
    public void write(FloatBuffer data) throws OpenCLException {
        VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, true, 0, data, null, null));
    }

    /**
     * Writes the specified data into this buffer.
     *
     * @param data The data to write
     * @throws OpenCLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, DoubleBuffer, PointerBuffer, PointerBuffer)
     */
    public void write(DoubleBuffer data) throws OpenCLException {
        VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, true, 0, data, null, null));
    }

    /**
     * Reads the data from this buffer into the specified store.
     *
     * @param store The buffer to store into
     * @throws OpenCLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, ByteBuffer, PointerBuffer, PointerBuffer)
     */
    public void read(ByteBuffer store) throws OpenCLException {
        VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, true, 0, store, null, null));
    }

    /**
     * Reads the data from this buffer into the specified store.
     *
     * @param store The buffer to store into
     * @throws OpenCLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, ShortBuffer, PointerBuffer, PointerBuffer)
     */
    public void read(ShortBuffer store) throws OpenCLException {
        VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, true, 0, store, null, null));
    }

    /**
     * Reads the data from this buffer into the specified store.
     *
     * @param store The buffer to store into
     * @throws OpenCLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, IntBuffer, PointerBuffer, PointerBuffer)
     */
    public void read(IntBuffer store) throws OpenCLException {
        VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, true, 0, store, null, null));
    }

    /**
     * Reads the data from this buffer into the specified store.
     *
     * @param store The buffer to store into
     * @throws OpenCLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, FloatBuffer, PointerBuffer, PointerBuffer)
     */
    public void read(FloatBuffer store) throws OpenCLException {
        VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, true, 0, store, null, null));
    }

    /**
     * Reads the data from this buffer into the specified store.
     *
     * @param store The buffer to store into
     * @throws OpenCLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, DoubleBuffer, PointerBuffer, PointerBuffer)
     */
    public void read(DoubleBuffer store) throws OpenCLException {
        VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, true, 0, store, null, null));
    }

    /**
     * Asynchronous implementation of {@link #write(ByteBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param data       The buffer with data to write
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws OpenCLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, ByteBuffer, PointerBuffer, PointerBuffer)
     */
    public void writeAsync(ByteBuffer data, @Nullable Runnable onComplete) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, false, 0, data, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), CL_COMPLETE, onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #write(ShortBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param data       The data to write
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws OpenCLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, short[], PointerBuffer, PointerBuffer)
     */
    public void writeAsync(ShortBuffer data, @Nullable Runnable onComplete) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, false, 0, data, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), CL_COMPLETE, onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #write(IntBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param data       The data to write
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws OpenCLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, int[], PointerBuffer, PointerBuffer)
     */
    public void writeAsync(IntBuffer data, @Nullable Runnable onComplete) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, false, 0, data, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), CL_COMPLETE, onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #write(FloatBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param data       The data to write
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws OpenCLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, float[], PointerBuffer, PointerBuffer)
     */
    public void writeAsync(FloatBuffer data, @Nullable Runnable onComplete) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, false, 0, data, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), CL_COMPLETE, onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #write(DoubleBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param data       The data to write
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws OpenCLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, double[], PointerBuffer, PointerBuffer)
     */
    public void writeAsync(DoubleBuffer data, @Nullable Runnable onComplete) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, false, 0, data, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), CL_COMPLETE, onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #read(ByteBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param store The buffer to store into
     * @throws OpenCLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, ByteBuffer, PointerBuffer, PointerBuffer)
     */
    public void readAsync(ByteBuffer store, @Nullable Runnable onComplete) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, false, 0, store, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), CL_COMPLETE, onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #read(ShortBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param store The buffer to store into
     * @throws OpenCLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, ShortBuffer, PointerBuffer, PointerBuffer)
     */
    public void readAsync(ShortBuffer store, @Nullable Runnable onComplete) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, false, 0, store, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), CL_COMPLETE, onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #read(IntBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param store The buffer to store into
     * @throws OpenCLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, IntBuffer, PointerBuffer, PointerBuffer)
     */
    public void readAsync(IntBuffer store, @Nullable Runnable onComplete) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, false, 0, store, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), CL_COMPLETE, onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #read(FloatBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param store The buffer to store into
     * @throws OpenCLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, FloatBuffer, PointerBuffer, PointerBuffer)
     */
    public void readAsync(FloatBuffer store, @Nullable Runnable onComplete) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, false, 0, store, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), CL_COMPLETE, onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #read(DoubleBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param store The buffer to store into
     * @throws OpenCLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, DoubleBuffer, PointerBuffer, PointerBuffer)
     */
    public void readAsync(DoubleBuffer store, @Nullable Runnable onComplete) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, false, 0, store, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), CL_COMPLETE, onComplete);
            }
        }
    }

    @Override
    public long pointer() {
        return this.pointer;
    }
}
