package foundry.veil.api.opencl;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL10GL;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;

import java.nio.IntBuffer;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.opencl.CL10GL.*;
import static org.lwjgl.opengl.ARBCLEvent.glCreateSyncFromCLeventARB;
import static org.lwjgl.opengl.GL11C.glFinish;
import static org.lwjgl.opengl.GL32C.glDeleteSync;
import static org.lwjgl.opengl.GL32C.glWaitSync;

/**
 * Manages the OpenCL kernel object. Buffers can be created with {@link #createBuffer(int, long)} and {@link #createBufferUnsafe(int, long)}
 *
 * @author Ocelot
 */
public class CLKernel implements NativeResource {

    private final CLEnvironment environment;
    private final ResourceLocation program;
    private final long handle;
    private final int maxWorkGroupSize;
    private final LongSet pointers;
    private final boolean legacySyncGLtoCL;
    private final boolean legacySyncCLtoGL;

    CLKernel(CLEnvironment environment, ResourceLocation program, long handle) throws CLException {
        this.environment = environment;
        this.program = program;
        this.handle = handle;
        this.pointers = new LongArraySet();
        this.legacySyncGLtoCL = !environment.getDevice().capabilities().cl_khr_gl_event;
        this.legacySyncCLtoGL = !RenderSystem.isOnRenderThread() || !GL.getCapabilities().GL_ARB_cl_event;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer work_group_loc = stack.mallocPointer(1);
            VeilOpenCL.checkCLError(clGetKernelWorkGroupInfo(this.handle, environment.getDevice().id(), CL_KERNEL_WORK_GROUP_SIZE, work_group_loc, null));
            this.maxWorkGroupSize = (int) work_group_loc.get(0);
        }
    }

    /**
     * Executes this kernel in 1 dimension. <code>globalWorkSize</code> must be evenly divisible by <code>localWorkSize</code>
     *
     * @param globalWorkSize The size of the global work group
     * @param localWorkSize  The size of each local work group
     * @throws CLException If any error occurs while executing the kernel
     * @see <a href="https://www.khronos.org/registry/OpenCL/sdk/2.1/docs/man/xhtml/clEnqueueNDRangeKernel.html">Reference Page</a>
     */
    public void execute(int globalWorkSize, int localWorkSize) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer global_work_size = stack.pointers(globalWorkSize);
            PointerBuffer local_work_size = stack.pointers(localWorkSize);
            VeilOpenCL.checkCLError(clEnqueueNDRangeKernel(this.environment.getCommandQueue(), this.handle, 1, null, global_work_size, local_work_size, null, null));
        }
    }

    /**
     * Executes this kernel in 2 dimensions. <code>globalWorkSize</code> must be evenly divisible by <code>localWorkSize</code>
     *
     * @param globalWorkSizeX The size of the global work group in the X
     * @param localWorkSizeX  The size of each local work group in the X
     * @param globalWorkSizeY The size of the global work group in the Y
     * @param localWorkSizeY  The size of each local work group in the Y
     * @throws CLException If any error occurs while executing the kernel
     * @see <a href="https://www.khronos.org/registry/OpenCL/sdk/2.1/docs/man/xhtml/clEnqueueNDRangeKernel.html">Reference Page</a>
     */
    public void execute(int globalWorkSizeX, int localWorkSizeX, int globalWorkSizeY, int localWorkSizeY) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer global_work_size = stack.pointers(globalWorkSizeX, globalWorkSizeY);
            PointerBuffer local_work_size = stack.pointers(localWorkSizeX, localWorkSizeY);
            VeilOpenCL.checkCLError(clEnqueueNDRangeKernel(this.environment.getCommandQueue(), this.handle, 2, null, global_work_size, local_work_size, null, null));
        }
    }

    /**
     * Executes this kernel in 3 dimensions. <code>globalWorkSize</code> must be evenly divisible by <code>localWorkSize</code>
     *
     * @param globalWorkSizeX The size of the global work group in the X
     * @param localWorkSizeX  The size of each local work group in the X
     * @param globalWorkSizeY The size of the global work group in the Y
     * @param localWorkSizeY  The size of each local work group in the Y
     * @param globalWorkSizeZ The size of the global work group in the Z
     * @param localWorkSizeZ  The size of each local work group in the Z
     * @throws CLException If any error occurs while executing the kernel
     * @see <a href="https://www.khronos.org/registry/OpenCL/sdk/2.1/docs/man/xhtml/clEnqueueNDRangeKernel.html">Reference Page</a>
     */
    public void execute(int globalWorkSizeX, int localWorkSizeX, int globalWorkSizeY, int localWorkSizeY, int globalWorkSizeZ, int localWorkSizeZ) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer global_work_size = stack.pointers(globalWorkSizeX, globalWorkSizeY, globalWorkSizeZ);
            PointerBuffer local_work_size = stack.pointers(localWorkSizeX, localWorkSizeY, localWorkSizeZ);
            VeilOpenCL.checkCLError(clEnqueueNDRangeKernel(this.environment.getCommandQueue(), this.handle, 3, null, global_work_size, local_work_size, null, null));
        }
    }

    /**
     * Executes this kernel in n dimensions. <code>globalWorkSize</code> must be evenly divisible by <code>localWorkSize</code>
     *
     * @param globalWorkSizes The size of each global work group
     * @param localWorkSizes  The size of each local work group
     * @throws CLException              If any error occurs while executing the kernel
     * @throws IllegalArgumentException If the length of <code>globalWorkSizes</code> and <code>localWorkSizes</code> are not equal
     * @see <a href="https://www.khronos.org/registry/OpenCL/sdk/2.1/docs/man/xhtml/clEnqueueNDRangeKernel.html">Reference Page</a>
     */
    public void execute(int[] globalWorkSizes, int[] localWorkSizes) throws CLException, IllegalArgumentException {
        if (globalWorkSizes.length != localWorkSizes.length) {
            throw new IllegalArgumentException("Global work size and local work size must have the same length");
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer global_work_size = stack.mallocPointer(globalWorkSizes.length);
            for (int i = 0; i < globalWorkSizes.length; i++) {
                global_work_size.put(i, globalWorkSizes[i]);
            }
            PointerBuffer local_work_size = stack.mallocPointer(localWorkSizes.length);
            for (int i = 0; i < localWorkSizes.length; i++) {
                local_work_size.put(i, localWorkSizes[i]);
            }
            VeilOpenCL.checkCLError(clEnqueueNDRangeKernel(this.environment.getCommandQueue(), this.handle, globalWorkSizes.length, null, global_work_size, local_work_size, null, null));
        }
    }

    /**
     * Acquires the data referenced by this object from OpenGL to allow OpenCL to safely modify it.
     *
     * @param objects The objects to acquire from OpenGL
     * @throws CLException If any error occurs while trying to sync data
     */
    public void acquireFromGL(CLMemObject... objects) throws CLException {
        if (!this.environment.requireManualInteropSync() || objects.length == 0) {
            return;
        }

        if (this.legacySyncGLtoCL) {
            glFinish();
        }

        long queue = this.environment.getCommandQueue();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pointers = stack.mallocPointer(objects.length);
            for (int i = 0; i < objects.length; i++) {
                pointers.put(i, objects[i].pointer());
            }
            VeilOpenCL.checkCLError(clEnqueueAcquireGLObjects(queue, pointers, null, null));
        }
    }

    /**
     * Releases the data referenced by this object from OpenCL to allow OpenGL to safely modify it again.
     *
     * @param objects The objects to acquire from OpenGL
     * @throws CLException If any error occurs while trying to sync data
     */
    public void releaseToGL(CLMemObject... objects) throws CLException {
        if (!this.environment.requireManualInteropSync() || objects.length == 0) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pointers = stack.mallocPointer(objects.length);
            for (int i = 0; i < objects.length; i++) {
                pointers.put(i, objects[i].pointer());
            }

            if (this.legacySyncCLtoGL) {
                VeilOpenCL.checkCLError(clEnqueueReleaseGLObjects(this.environment.getCommandQueue(), pointers, null, null));
                this.environment.finish();
                return;
            }

            PointerBuffer syncBuffer = stack.mallocPointer(1);
            VeilOpenCL.checkCLError(clEnqueueReleaseGLObjects(this.environment.getCommandQueue(), pointers, null, syncBuffer));

            long event = syncBuffer.get(0);
            long glFenceFromCLEvent = glCreateSyncFromCLeventARB(this.environment.getContext(), event, 0);
            glWaitSync(glFenceFromCLEvent, 0, 0);
            glDeleteSync(glFenceFromCLEvent);

            VeilOpenCL.checkCLError(clReleaseEvent(event));
        }
    }

    /**
     * Creates a new CL memory buffer. Any errors are consumed and printed to console.
     *
     * @param flags a bit-field that is used to specify allocation and usage information such as the memory area that should be used to allocate the buffer object and
     *              how it will be used. If value specified for flags is 0, the default is used which is {@link CL10#CL_MEM_READ_WRITE MEM_READ_WRITE}. One of:<br>
     *              <table>
     *                  <caption>All possible OpenCL memory buffer flags</caption>
     *                  <tr>
     *                      <td>{@link CL10#CL_MEM_READ_WRITE MEM_READ_WRITE}</td>
     *                      <td>{@link CL10#CL_MEM_WRITE_ONLY MEM_WRITE_ONLY}</td>
     *                      <td>{@link CL10#CL_MEM_READ_ONLY MEM_READ_ONLY}</td>
     *                      <td>{@link CL10#CL_MEM_USE_HOST_PTR MEM_USE_HOST_PTR}</td>
     *                      <td>{@link CL10#CL_MEM_ALLOC_HOST_PTR MEM_ALLOC_HOST_PTR}</td>
     *                  </tr>
     *                  <tr>
     *                      <td>{@link CL10#CL_MEM_COPY_HOST_PTR MEM_COPY_HOST_PTR}</td>
     *                      <td>{@link CL12#CL_MEM_HOST_WRITE_ONLY MEM_HOST_WRITE_ONLY}</td>
     *                      <td>{@link CL12#CL_MEM_HOST_READ_ONLY MEM_HOST_READ_ONLY}</td>
     *                      <td>{@link CL12#CL_MEM_HOST_NO_ACCESS MEM_HOST_NO_ACCESS}</td>
     *                  </tr>
     *              </table>
     * @param size  The size of the buffer in bytes
     * @return A data buffer that can be used with {@link #setPointers(int, long...)} or {@link #setPointers(int, CLMemObject...)} or <code>null</code> if an error occurred
     * @see CL10#clCreateBuffer(long, long, long, IntBuffer)
     */
    public @Nullable CLBuffer createBufferUnsafe(int flags, long size) {
        try {
            return this.createBuffer(flags, size);
        } catch (CLException e) {
            VeilOpenCL.LOGGER.error("Failed to create CL buffer", e);
            return null;
        }
    }

    /**
     * Creates a new CL memory buffer.
     *
     * @param flags a bit-field that is used to specify allocation and usage information such as the memory area that should be used to allocate the buffer object and
     *              how it will be used. If value specified for flags is 0, the default is used which is {@link CL10#CL_MEM_READ_WRITE MEM_READ_WRITE}. One of:<br>
     *              <table>
     *                  <caption>OpenCL memory buffer flags</caption>
     *                  <tr>
     *                      <td>{@link CL10#CL_MEM_READ_WRITE MEM_READ_WRITE}</td>
     *                      <td>{@link CL10#CL_MEM_WRITE_ONLY MEM_WRITE_ONLY}</td>
     *                      <td>{@link CL10#CL_MEM_READ_ONLY MEM_READ_ONLY}</td>
     *                      <td>{@link CL10#CL_MEM_USE_HOST_PTR MEM_USE_HOST_PTR}</td>
     *                      <td>{@link CL10#CL_MEM_ALLOC_HOST_PTR MEM_ALLOC_HOST_PTR}</td>
     *                  </tr>
     *                  <tr>
     *                      <td>{@link CL10#CL_MEM_COPY_HOST_PTR MEM_COPY_HOST_PTR}</td>
     *                      <td>{@link CL12#CL_MEM_HOST_WRITE_ONLY MEM_HOST_WRITE_ONLY}</td>
     *                      <td>{@link CL12#CL_MEM_HOST_READ_ONLY MEM_HOST_READ_ONLY}</td>
     *                      <td>{@link CL12#CL_MEM_HOST_NO_ACCESS MEM_HOST_NO_ACCESS}</td>
     *                  </tr>
     *              </table>
     * @param size  The size of the buffer in bytes
     * @return A data buffer that can be used with {@link #setPointers(int, long...)} or {@link #setPointers(int, CLMemObject...)}
     * @throws CLException If there is any problem creating the buffer
     * @see CL10#clCreateBuffer(long, long, long, IntBuffer)
     */
    public CLBuffer createBuffer(int flags, long size) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer error_ret = stack.mallocInt(1);
            long pointer = clCreateBuffer(this.environment.getContext(), flags, size, error_ret);
            VeilOpenCL.checkCLError(error_ret.get(0));
            this.pointers.add(pointer);
            return new CLBuffer(this, pointer);
        }
    }

    /**
     * Creates a new CL memory buffer that reflects the data in an OpenGL buffer. Any errors are consumed and printed to console.
     *
     * @param flags  a bit-field that is used to specify allocation and usage information such as the memory area that should be used to allocate the buffer object and
     *               how it will be used. If value specified for flags is 0, the default is used which is {@link CL10#CL_MEM_READ_WRITE MEM_READ_WRITE}. One of:<br>
     *               <table>
     *                   <caption>OpenCL memory buffer flags</caption>
     *                   <tr>
     *                       <td>{@link CL10#CL_MEM_READ_WRITE MEM_READ_WRITE}</td>
     *                       <td>{@link CL10#CL_MEM_WRITE_ONLY MEM_WRITE_ONLY}</td>
     *                       <td>{@link CL10#CL_MEM_READ_ONLY MEM_READ_ONLY}</td>
     *                   </tr>
     *               </table>
     * @param buffer The OpenGL buffer to attach to
     * @return A data buffer that can be used with {@link #setPointers(int, long...)} or {@link #setPointers(int, CLMemObject...)} or <code>null</code> if an error occurred
     * @see CL10GL#clCreateFromGLBuffer(long, long, int, IntBuffer)
     */
    public @Nullable CLBuffer createBufferFromGLUnsafe(int flags, int buffer) {
        try {
            return this.createBufferFromGL(flags, buffer);
        } catch (CLException e) {
            VeilOpenCL.LOGGER.error("Failed to create CL buffer", e);
            return null;
        }
    }

    /**
     * Creates a new CL memory buffer that reflects the data in an OpenGL buffer.
     *
     * @param flags  a bit-field that is used to specify allocation and usage information such as the memory area that should be used to allocate the buffer object and
     *               how it will be used. If value specified for flags is 0, the default is used which is {@link CL10#CL_MEM_READ_WRITE MEM_READ_WRITE}. One of:<br>
     *               <table>
     *                   <caption>OpenCL memory buffer flags</caption>
     *                   <tr>
     *                       <td>{@link CL10#CL_MEM_READ_WRITE MEM_READ_WRITE}</td>
     *                       <td>{@link CL10#CL_MEM_WRITE_ONLY MEM_WRITE_ONLY}</td>
     *                       <td>{@link CL10#CL_MEM_READ_ONLY MEM_READ_ONLY}</td>
     *                   </tr>
     *               </table>
     * @param buffer The OpenGL buffer to attach to
     * @return A data buffer that can be used with {@link #setPointers(int, long...)} or {@link #setPointers(int, CLMemObject...)}
     * @throws CLException If there is any problem creating the buffer
     * @see CL10GL#clCreateFromGLBuffer(long, long, int, IntBuffer)
     */
    public CLBuffer createBufferFromGL(int flags, int buffer) throws CLException {
        if (this.environment.isOpenGLSupported() && !this.environment.requireManualInteropSync()) {
            RenderSystem.assertOnRenderThread();
            glFinish(); // Synchronize the GL data for CL
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer error_ret = stack.mallocInt(1);
            long pointer = clCreateFromGLBuffer(this.environment.getContext(), flags, buffer, error_ret);
            VeilOpenCL.checkCLError(error_ret.get(0));
            this.pointers.add(pointer);
            return new CLBuffer(this, pointer);
        }
    }

    /**
     * Sets a single byte parameter.
     *
     * @param index The index to set the parameter for
     * @param value The value of the parameter
     * @throws CLException If there is any problem setting the kernel argument
     */
    public void setByte(int index, byte value) throws CLException {
        VeilOpenCL.checkCLError(clSetKernelArg1b(this.handle, index, value));
    }

    /**
     * Sets a single short parameter.
     *
     * @param index The index to set the parameter for
     * @param value The value of the parameter
     * @throws CLException If there is any problem setting the kernel argument
     */
    public void setShort(int index, short value) throws CLException {
        VeilOpenCL.checkCLError(clSetKernelArg1s(this.handle, index, value));
    }

    /**
     * Sets a single int parameter.
     *
     * @param index The index to set the parameter for
     * @param value The value of the parameter
     * @throws CLException If there is any problem setting the kernel argument
     */
    public void setInt(int index, int value) throws CLException {
        VeilOpenCL.checkCLError(clSetKernelArg1i(this.handle, index, value));
    }

    /**
     * Sets a single long parameter.
     *
     * @param index The index to set the parameter for
     * @param value The value of the parameter
     * @throws CLException If there is any problem setting the kernel argument
     */
    public void setLong(int index, long value) throws CLException {
        VeilOpenCL.checkCLError(clSetKernelArg1l(this.handle, index, value));
    }

    /**
     * Sets a single float parameter.
     *
     * @param index The index to set the parameter for
     * @param value The value of the parameter
     * @throws CLException If there is any problem setting the kernel argument
     */
    public void setFloat(int index, float value) throws CLException {
        VeilOpenCL.checkCLError(clSetKernelArg1f(this.handle, index, value));
    }

    /**
     * Sets a single float2 parameter.
     *
     * @param index The index to set the parameter for
     * @param value The value of the parameter
     * @throws CLException If there is any problem setting the kernel argument
     */
    public void setVector2f(int index, Vector2fc value) throws CLException {
        this.setVector2f(index, value.x(), value.y());
    }

    /**
     * Sets a single float2 parameter.
     *
     * @param index The index to set the parameter for
     * @param x     The X value of the parameter
     * @param y     The Y value of the parameter
     * @throws CLException If there is any problem setting the kernel argument
     */
    public void setVector2f(int index, float x, float y) throws CLException {
        VeilOpenCL.checkCLError(clSetKernelArg2f(this.handle, index, x, y));
    }

    /**
     * Sets a single float4 parameter.
     *
     * @param index The index to set the parameter for
     * @param value The value of the parameter
     * @throws CLException If there is any problem setting the kernel argument
     */
    public void setVector3f(int index, Vector3fc value) throws CLException {
        this.setVector4f(index, value.x(), value.y(), value.z(), 0);
    }

    /**
     * Sets a single float4 parameter.
     *
     * @param index The index to set the parameter for
     * @param value The value of the parameter
     * @throws CLException If there is any problem setting the kernel argument
     */
    public void setVector4f(int index, Vector4fc value) throws CLException {
        this.setVector4f(index, value.x(), value.y(), value.z(), value.z());
    }

    /**
     * Sets a single float4 parameter.
     *
     * @param index The index to set the parameter for
     * @param x     The X value of the parameter
     * @param y     The Y value of the parameter
     * @param z     The Z value of the parameter
     * @param w     The W value of the parameter
     * @throws CLException If there is any problem setting the kernel argument
     */
    public void setVector4f(int index, float x, float y, float z, float w) throws CLException {
        VeilOpenCL.checkCLError(clSetKernelArg4f(this.handle, index, x, y, z, w));
    }

    /**
     * Sets a single double parameter.
     *
     * @param index The index to set the parameter for
     * @param value The value of the parameter
     * @throws CLException If there is any problem setting the kernel argument
     */
    public void setDouble(int index, double value) throws CLException {
        VeilOpenCL.checkCLError(clSetKernelArg1d(this.handle, index, value));
    }

    /**
     * Sets an array of pointers to the specified parameter.
     *
     * @param index The index to set the parameter for
     * @param value The values of the parameter
     * @throws CLException If there is any problem setting the kernel argument
     */
    public void setPointers(int index, long... value) throws CLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer arg_value = stack.pointers(value);
            VeilOpenCL.checkCLError(clSetKernelArg(this.handle, index, arg_value));
        }
    }

    /**
     * Sets an array of memory object pointers to the specified parameter.
     *
     * @param index The index to set the parameter for
     * @param value The values of the parameter
     * @throws CLException If there is any problem setting the kernel argument
     */
    public void setPointers(int index, CLMemObject... value) throws CLException {
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
     * @return The environment this kernel is in
     */
    public CLEnvironment getEnvironment() {
        return this.environment;
    }

    /**
     * @return The name of the program this kernel is created from
     */
    public ResourceLocation getProgram() {
        return this.program;
    }

    /**
     * @return The pointer to the kernel object
     */
    public long getHandle() {
        return this.handle;
    }

    /**
     * @return The maximum size a work group can be
     */
    public int getMaxWorkGroupSize() {
        return this.maxWorkGroupSize;
    }

    @Override
    public void free() {
        clReleaseKernel(this.handle);
        this.pointers.forEach(CL10::clReleaseMemObject);
        this.pointers.clear();
        this.environment.free(this);
    }

    @ApiStatus.Internal
    void free(CLMemObject object) {
        long pointer = object.pointer();
        this.pointers.remove(pointer);
        clReleaseMemObject(pointer);
    }
}
