package foundry.veil.editor;

import com.mojang.logging.LogUtils;
import foundry.veil.imgui.CodeEditor;
import foundry.veil.opencl.VeilOpenCL;
import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiSliderFlags;
import imgui.type.ImInt;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.opencl.CL20.clCreateCommandQueueWithProperties;

public class OpenCLEditor extends SingleWindowEditor {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final CodeEditor codeEditor;
    private String source;

    private long context = 0;
    private long queue = 0;
    private long program = 0;
    private long kernel = 0;
    private LongSet buffers = new LongArraySet(3);
    private CLContextCallback errorCallback;

    private VeilOpenCL.DeviceInfo deviceInfo;

    private final ImInt elements = new ImInt(64);
    private final ImInt workGroups = new ImInt(16);

    public OpenCLEditor() {
        this.codeEditor = new CodeEditor("Save");
        this.codeEditor.setSaveCallback((source, errorConsumer) -> this.compileProgram(source));
        this.source = """
                void kernel test_add(global const int* A, global const int* B, global int* C) {
                    int i = get_global_id(0);
                    C[i] = A[i] + B[i];
                }""";

        this.errorCallback = null;
        this.deviceInfo = null;
    }

    private boolean selectDevice(VeilOpenCL.DeviceInfo deviceInfo) {
        if (Objects.equals(this.deviceInfo, deviceInfo)) {
            return false;
        }

        if (!deviceInfo.compilerAvailable()) {
            return false;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer ctxProps = stack.mallocPointer(3);
            ctxProps
                    .put(0, CL_CONTEXT_PLATFORM)
                    .put(1, deviceInfo.platform())
                    .put(2, 0);

            long device = deviceInfo.id();

            CLContextCallback errorCallback = CLContextCallback.create((errinfo, private_info, cb, user_data) -> {
                LOGGER.error("[LWJGL] cl_context_callback");
                LOGGER.error("\tInfo: " + MemoryUtil.memUTF8(errinfo));
            });
            IntBuffer errcode_ret = stack.callocInt(1);
            long context = 0;
            long queue = 0;

            try {
                context = clCreateContext(ctxProps, device, errorCallback, MemoryUtil.NULL, errcode_ret);
                VeilOpenCL.checkCLError(errcode_ret);

                // Create command queue
                queue = clCreateCommandQueueWithProperties(context, device, null, errcode_ret);
                VeilOpenCL.checkCLError(errcode_ret);

                if (queue == MemoryUtil.NULL) {
                    throw new IllegalStateException("Failed to create OpenCL queue");
                }

                this.free();

                this.context = context;
                this.queue = queue;
                this.deviceInfo = deviceInfo;
                this.errorCallback = errorCallback;

                return true;
            } catch (Exception e) {
                LOGGER.error("Failed to change CL device", e);

                errorCallback.free();
                if (queue != 0) {
                    clReleaseCommandQueue(queue);
                }
                if (context != 0) {
                    clReleaseContext(context);
                }
            }
        }

        return false;
    }

    private boolean compileProgram(String source) {
        if (this.deviceInfo == null) {
            return false;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            long device = this.deviceInfo.id();

            IntBuffer errcode_ret = stack.callocInt(1);

            long program = 0;
            long kernel = 0;

            try {
                program = clCreateProgramWithSource(this.context, source, errcode_ret);
                VeilOpenCL.checkCLError(errcode_ret);

                int programStatus = clBuildProgram(program, device, "", null, 0);
                if (programStatus != CL_SUCCESS) {
                    System.err.println(VeilOpenCL.getProgramBuildInfo(program, device, CL_PROGRAM_BUILD_LOG));
                    throw new IOException("Failed to compile program");
                }

                kernel = clCreateKernel(program, "test_add", errcode_ret);
                if (errcode_ret.get(0) == CL_INVALID_KERNEL_NAME) {
                    throw new IllegalStateException("Failed to find kernel: test_add");
                }
                VeilOpenCL.checkCLError(errcode_ret);

                this.source = source;
                this.program = program;
                this.kernel = kernel;
                return true;
            } catch (Exception e) {
                LOGGER.error("Failed to compile program", e);
                if (kernel != 0) {
                    clReleaseKernel(kernel);
                }
                if (program != 0) {
                    clReleaseProgram(program);
                }
            }
        }

        return false;
    }

    @Override
    protected void renderComponents() {
        VeilOpenCL.PlatformInfo[] platforms = VeilOpenCL.get().getPlatforms();
        if (platforms.length == 0) {
            ImGui.text("No platforms found");
            return;
        }

        if (ImGui.beginCombo("Device", this.deviceInfo == null ? "No Device Selected" : this.deviceInfo.name())) {
            for (VeilOpenCL.PlatformInfo platform : platforms) {
                for (VeilOpenCL.DeviceInfo device : platform.devices()) {
                    if (ImGui.selectable(platform.vendor() + " " + device.name(), this.deviceInfo == device)) {
                        this.selectDevice(device);
                    }
                }
            }
            ImGui.endCombo();
        }

        ImGui.beginDisabled(this.deviceInfo == null);
        if (ImGui.button("Edit Source")) {
            this.codeEditor.show(this.source);
        }
        ImGui.endDisabled();

        ImGui.beginDisabled(this.program == 0);
        ImGui.sameLine();
        if (ImGui.button("Run")) {
            LongSet buffers = new LongArraySet(3);

            int LIST_SIZE = this.elements.get();
            int[] A = new int[LIST_SIZE];
            int[] B = new int[LIST_SIZE];
            int[] C = new int[LIST_SIZE];
            for (int i = 0; i < LIST_SIZE; i++) {
                A[i] = i;
                B[i] = LIST_SIZE - i;
                C[i] = 2 * i;
            }

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer errcode_ret = stack.callocInt(1);

                long memObjectA = clCreateBuffer(this.context, CL_MEM_READ_ONLY, (long) LIST_SIZE * Integer.BYTES, errcode_ret);
                VeilOpenCL.checkCLError(errcode_ret);
                buffers.add(memObjectA);

                long memObjectB = clCreateBuffer(this.context, CL_MEM_READ_ONLY, (long) LIST_SIZE * Integer.BYTES, errcode_ret);
                VeilOpenCL.checkCLError(errcode_ret);
                buffers.add(memObjectB);

                long memObjectC = clCreateBuffer(this.context, CL_MEM_READ_ONLY, (long) LIST_SIZE * Integer.BYTES, errcode_ret);
                VeilOpenCL.checkCLError(errcode_ret);
                buffers.add(memObjectC);

                long memObjectD = clCreateBuffer(this.context, CL_MEM_WRITE_ONLY, (long) LIST_SIZE * Integer.BYTES, errcode_ret);
                VeilOpenCL.checkCLError(errcode_ret);
                buffers.add(memObjectD);

                VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.queue, memObjectA, true, 0, A, null, null));
                VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.queue, memObjectB, true, 0, B, null, null));
                VeilOpenCL.checkCLError(clEnqueueWriteBuffer(this.queue, memObjectC, true, 0, C, null, null));

                VeilOpenCL.checkCLError(clSetKernelArg1p(this.kernel, 0, memObjectA));
                VeilOpenCL.checkCLError(clSetKernelArg1p(this.kernel, 1, memObjectB));
                VeilOpenCL.checkCLError(clSetKernelArg1p(this.kernel, 2, memObjectC));
                VeilOpenCL.checkCLError(clSetKernelArg1p(this.kernel, 3, memObjectD));

                PointerBuffer work_group_loc = stack.mallocPointer(1);
                VeilOpenCL.checkCLError(clGetKernelWorkGroupInfo(this.kernel, this.deviceInfo.id(), CL_KERNEL_WORK_GROUP_SIZE, work_group_loc, null));

                PointerBuffer global_work_size = stack.mallocPointer(1);
                global_work_size.put(0, LIST_SIZE);
                PointerBuffer local_work_size = stack.mallocPointer(1);
                local_work_size.put(0, Math.min(this.workGroups.get(), LIST_SIZE));
                VeilOpenCL.checkCLError(clEnqueueNDRangeKernel(this.queue, this.kernel, 1, null, global_work_size, local_work_size, null, null));

                int[] D = new int[LIST_SIZE];
                VeilOpenCL.checkCLError(clEnqueueReadBuffer(this.queue, memObjectD, true, 0, D, null, null));

                VeilOpenCL.checkCLError(clFlush(this.queue));
                VeilOpenCL.checkCLError(clFinish(this.queue));

                System.out.println("Done");
            } catch (Exception e) {
                LOGGER.error("Failed to run OpenCL", e);
            } finally {
                for (long buffer : buffers) {
                    if (buffer != 0) {
                        VeilOpenCL.checkCLError(clReleaseMemObject(buffer));
                    }
                }
            }
        }
        ImGui.endDisabled();

        ImGui.dragScalar("Elements", ImGuiDataType.U16, this.elements, 2, 0, 100_000_000);

        ImGui.beginDisabled(this.deviceInfo == null);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int max = Integer.MAX_VALUE;
            if (this.kernel != MemoryUtil.NULL && this.deviceInfo != null) {
                PointerBuffer work_group_loc = stack.mallocPointer(1);
                VeilOpenCL.checkCLError(clGetKernelWorkGroupInfo(this.kernel, this.deviceInfo.id(), CL_KERNEL_WORK_GROUP_SIZE, work_group_loc, null));
                max = (int) work_group_loc.get(0);
            }
            ImGui.sliderScalar("Local Work Groups", ImGuiDataType.U32, this.workGroups, 2, max);
        }
        ImGui.endDisabled();
    }

    @Override
    public void free() {
        super.free();

        this.deviceInfo = null;
        if (this.kernel != 0) {
            clReleaseKernel(this.kernel);
        }
        if (this.program != 0) {
            clReleaseProgram(this.program);
        }
        for (long buffer : this.buffers) {
            if (buffer != 0) {
                clReleaseMemObject(buffer);
            }
        }
        if (this.queue != 0) {
            clReleaseCommandQueue(this.queue);
        }
        if (this.context != 0) {
            clReleaseContext(this.context);
        }
        if (this.errorCallback != null) {
            this.errorCallback.free();
            this.errorCallback = null;
        }

        this.program = 0;
        this.kernel = 0;
        this.buffers.clear();
        this.queue = 0;
        this.context = 0;
    }

    @Override
    public void render() {
        super.render();
        this.codeEditor.renderWindow();
    }

    @Override
    public String getDisplayName() {
        return "OpenCL Editor";
    }
}
