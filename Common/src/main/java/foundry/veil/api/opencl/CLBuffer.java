package foundry.veil.api.opencl;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.system.MemoryStack;

import java.nio.*;

import static org.lwjgl.opencl.CL10.clEnqueueReadBuffer;
import static org.lwjgl.opencl.CL10.clEnqueueWriteBuffer;

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

    private final CLKernel kernel;
    private final CLEnvironment environment;
    private final long pointer;

    CLBuffer(CLKernel kernel, long pointer) {
        this.kernel = kernel;
        this.environment = kernel.getEnvironment();
        this.pointer = pointer;
    }

    /**
     * Writes the specified data into this buffer.
     *
     * @param offset The offset into this buffer to start writing data to
     * @param data   The buffer with data to write
     * @throws CLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, ByteBuffer, PointerBuffer, PointerBuffer)
     */
    public void write(long offset, ByteBuffer data) throws CLException {
        VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, true, offset, data, null, null));
    }

    /**
     * Writes the specified data into this buffer.
     *
     * @param offset The offset into this buffer to start writing data to
     * @param data   The data to write
     * @throws CLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, ShortBuffer, PointerBuffer, PointerBuffer)
     */
    public void write(long offset, ShortBuffer data) throws CLException {
        VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, true, offset, data, null, null));
    }

    /**
     * Writes the specified data into this buffer.
     *
     * @param offset The offset into this buffer to start writing data to
     * @param data   The data to write
     * @throws CLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, IntBuffer, PointerBuffer, PointerBuffer)
     */
    public void write(long offset, IntBuffer data) throws CLException {
        VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, true, offset, data, null, null));
    }

    /**
     * Writes the specified data into this buffer.
     *
     * @param offset The offset into this buffer to start writing data to
     * @param data   The data to write
     * @throws CLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, FloatBuffer, PointerBuffer, PointerBuffer)
     */
    public void write(long offset, FloatBuffer data) throws CLException {
        VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, true, offset, data, null, null));
    }

    /**
     * Writes the specified data into this buffer.
     *
     * @param offset The offset into this buffer to start writing data to
     * @param data   The data to write
     * @throws CLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, DoubleBuffer, PointerBuffer, PointerBuffer)
     */
    public void write(long offset, DoubleBuffer data) throws CLException {
        VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, true, offset, data, null, null));
    }

    /**
     * Reads the data from this buffer into the specified store.
     *
     * @param offset The offset into this buffer to start reading data from
     * @param store  The buffer to store into
     * @throws CLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, ByteBuffer, PointerBuffer, PointerBuffer)
     */
    public void read(long offset, ByteBuffer store) throws CLException {
        VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, true, offset, store, null, null));
    }

    /**
     * Reads the data from this buffer into the specified store.
     *
     * @param offset The offset into this buffer to start reading data from
     * @param store  The buffer to store into
     * @throws CLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, ShortBuffer, PointerBuffer, PointerBuffer)
     */
    public void read(long offset, ShortBuffer store) throws CLException {
        VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, true, offset, store, null, null));
    }

    /**
     * Reads the data from this buffer into the specified store.
     *
     * @param offset The offset into this buffer to start reading data from
     * @param store  The buffer to store into
     * @throws CLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, IntBuffer, PointerBuffer, PointerBuffer)
     */
    public void read(long offset, IntBuffer store) throws CLException {
        VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, true, offset, store, null, null));
    }

    /**
     * Reads the data from this buffer into the specified store.
     *
     * @param offset The offset into this buffer to start reading data from
     * @param store  The buffer to store into
     * @throws CLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, FloatBuffer, PointerBuffer, PointerBuffer)
     */
    public void read(long offset, FloatBuffer store) throws CLException {
        VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, true, offset, store, null, null));
    }

    /**
     * Reads the data from this buffer into the specified store.
     *
     * @param offset The offset into this buffer to start reading data from
     * @param store  The buffer to store into
     * @throws CLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, DoubleBuffer, PointerBuffer, PointerBuffer)
     */
    public void read(long offset, DoubleBuffer store) throws CLException {
        VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, true, offset, store, null, null));
    }

    /**
     * Asynchronous implementation of {@link #write(long, ByteBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param offset     The offset into this buffer to start writing data to
     * @param data       The buffer with data to write
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws CLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, ByteBuffer, PointerBuffer, PointerBuffer)
     */
    public void writeAsync(long offset, ByteBuffer data, @Nullable Runnable onComplete) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, false, offset, data, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #write(long, ShortBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param offset     The offset into this buffer to start writing data to
     * @param data       The data to write
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws CLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, short[], PointerBuffer, PointerBuffer)
     */
    public void writeAsync(long offset, ShortBuffer data, @Nullable Runnable onComplete) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, false, offset, data, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #write(long, IntBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param offset     The offset into this buffer to start writing data to
     * @param data       The data to write
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws CLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, int[], PointerBuffer, PointerBuffer)
     */
    public void writeAsync(long offset, IntBuffer data, @Nullable Runnable onComplete) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, false, offset, data, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #write(long, FloatBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param offset     The offset into this buffer to start writing data to
     * @param data       The data to write
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws CLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, float[], PointerBuffer, PointerBuffer)
     */
    public void writeAsync(long offset, FloatBuffer data, @Nullable Runnable onComplete) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, false, offset, data, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #write(long, DoubleBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param offset     The offset into this buffer to start writing data to
     * @param data       The data to write
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws CLException If any error occurs while trying to upload data
     * @see CL10#clEnqueueWriteBuffer(long, long, boolean, long, double[], PointerBuffer, PointerBuffer)
     */
    public void writeAsync(long offset, DoubleBuffer data, @Nullable Runnable onComplete) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.environment.getCommandQueue(), this.pointer, false, offset, data, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #read(long, ByteBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param offset     The offset into this buffer to start reading data from
     * @param store      The buffer to store into
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws CLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, ByteBuffer, PointerBuffer, PointerBuffer)
     */
    public void readAsync(long offset, ByteBuffer store, @Nullable Runnable onComplete) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, false, offset, store, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #read(long, ShortBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param offset     The offset into this buffer to start reading data from
     * @param store      The buffer to store into
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws CLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, ShortBuffer, PointerBuffer, PointerBuffer)
     */
    public void readAsync(long offset, ShortBuffer store, @Nullable Runnable onComplete) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, false, offset, store, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #read(long, IntBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param offset     The offset into this buffer to start reading data from
     * @param store      The buffer to store into
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws CLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, IntBuffer, PointerBuffer, PointerBuffer)
     */
    public void readAsync(long offset, IntBuffer store, @Nullable Runnable onComplete) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, false, offset, store, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #read(long, FloatBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param offset     The offset into this buffer to start reading data from
     * @param store      The buffer to store into
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws CLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, FloatBuffer, PointerBuffer, PointerBuffer)
     */
    public void readAsync(long offset, FloatBuffer store, @Nullable Runnable onComplete) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, false, offset, store, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), onComplete);
            }
        }
    }

    /**
     * Asynchronous implementation of {@link #read(long, DoubleBuffer)}. The specified callback will be fired when the operation completes.
     *
     * @param offset     The offset into this buffer to start reading data from
     * @param store      The buffer to store into
     * @param onComplete The callback for when the operation completes or <code>null</code>
     * @throws CLException If any error occurs while trying to download data
     * @see CL10#clEnqueueReadBuffer(long, long, boolean, long, DoubleBuffer, PointerBuffer, PointerBuffer)
     */
    public void readAsync(long offset, DoubleBuffer store, @Nullable Runnable onComplete) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer event = onComplete != null ? stack.mallocPointer(1) : null;
            VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.environment.getCommandQueue(), this.pointer, false, offset, store, null, event));
            if (event != null) {
                this.environment.getEventDispatcher().listen(event.get(0), onComplete);
            }
        }
    }

    @Override
    public long pointer() {
        return this.pointer;
    }

    @Override
    public void free() {
        this.kernel.free(this);
    }
}
