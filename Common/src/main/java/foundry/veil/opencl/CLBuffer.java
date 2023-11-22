package foundry.veil.opencl;

import java.nio.ByteBuffer;

import static org.lwjgl.opencl.CL10.clEnqueueReadBuffer;
import static org.lwjgl.opencl.CL10.clEnqueueWriteBuffer;

public class CLBuffer implements CLMemObject {

    private final OpenCLEnvironment environment;
    private final long pointer;

    CLBuffer(OpenCLEnvironment environment, long pointer) {
        this.environment = environment;
        this.pointer = pointer;
    }

    public void write(ByteBuffer data) {
        clEnqueueWriteBuffer(this.environment.getQueue(), this.pointer, true, 0, data, null, null);
    }

    public void write(short... data) {
        clEnqueueWriteBuffer(this.environment.getQueue(), this.pointer, true, 0, data, null, null);
    }

    public void write(int... data) {
        clEnqueueWriteBuffer(this.environment.getQueue(), this.pointer, true, 0, data, null, null);
    }

    public void write(float... data) {
        clEnqueueWriteBuffer(this.environment.getQueue(), this.pointer, true, 0, data, null, null);
    }

    public void write(double... data) {
        clEnqueueWriteBuffer(this.environment.getQueue(), this.pointer, true, 0, data, null, null);
    }

    public void read(ByteBuffer store) {
        clEnqueueReadBuffer(this.environment.getQueue(), this.pointer, true, 0, store, null, null);
    }

    public void read(short[] store) {
        clEnqueueReadBuffer(this.environment.getQueue(), this.pointer, true, 0, store, null, null);
    }

    public void read(int[] store) {
        clEnqueueReadBuffer(this.environment.getQueue(), this.pointer, true, 0, store, null, null);
    }

    public void read(float[] store) {
        clEnqueueReadBuffer(this.environment.getQueue(), this.pointer, true, 0, store, null, null);
    }

    public void read(double[] store) {
        clEnqueueReadBuffer(this.environment.getQueue(), this.pointer, true, 0, store, null, null);
    }

    @Override
    public long pointer() {
        return this.pointer;
    }
}
