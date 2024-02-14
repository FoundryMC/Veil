package foundry.veil.api.opencl;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opencl.CL10.clReleaseEvent;
import static org.lwjgl.opencl.CL10GL.clEnqueueAcquireGLObjects;
import static org.lwjgl.opencl.CL10GL.clEnqueueReleaseGLObjects;
import static org.lwjgl.opengl.ARBCLEvent.glCreateSyncFromCLeventARB;
import static org.lwjgl.opengl.GL11C.glFinish;
import static org.lwjgl.opengl.GL32C.glDeleteSync;
import static org.lwjgl.opengl.GL32C.glWaitSync;

/**
 * A {@link CLBuffer} that references buffers created using OpenGL.
 *
 * @see CLBuffer
 */
public class CLGLBuffer extends CLBuffer implements CLGLMemObject {

    private final boolean legacySyncGLtoCL;
    private final boolean legacySyncCLtoGL;

    CLGLBuffer(CLKernel kernel, long pointer) {
        super(kernel, pointer);
        this.legacySyncGLtoCL = !kernel.getEnvironment().getDevice().capabilities().cl_khr_gl_event;
        this.legacySyncCLtoGL = !GL.getCapabilities().GL_ARB_cl_event;
    }

    @Override
    public void acquireFromGL() throws CLException {
        if (this.legacySyncGLtoCL) {
            glFinish();
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VeilOpenCL.checkCLError(clEnqueueAcquireGLObjects(this.environment.getCommandQueue(), stack.pointers(this.pointer), null, null));
        }
    }

    @Override
    public void releaseToGL() throws CLException {
        if (this.legacySyncCLtoGL) {
            this.environment.finish();
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer syncBuffer = stack.mallocPointer(1);
            VeilOpenCL.checkCLError(clEnqueueReleaseGLObjects(this.environment.getCommandQueue(), stack.pointers(this.pointer), null, syncBuffer));

            long event = syncBuffer.get(0);
            long glFenceFromCLEvent = glCreateSyncFromCLeventARB(this.environment.getContext(), event, 0);
            glWaitSync(glFenceFromCLEvent, 0, 0);
            glDeleteSync(glFenceFromCLEvent);

            clReleaseEvent(event);
        }
    }
}
