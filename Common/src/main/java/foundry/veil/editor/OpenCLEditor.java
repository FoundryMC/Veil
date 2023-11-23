package foundry.veil.editor;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import foundry.veil.imgui.CodeEditor;
import foundry.veil.opencl.*;
import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import java.nio.IntBuffer;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.opencl.CL10.CL_MEM_READ_ONLY;
import static org.lwjgl.opencl.CL10.CL_MEM_WRITE_ONLY;

public class OpenCLEditor extends SingleWindowEditor {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final CodeEditor codeEditor;
    private String source;

    private CLEnvironment environment;
    private CLKernel kernel;

    private CLBuffer bufferA;
    private CLBuffer bufferB;
    private CLBuffer bufferC;
    private CLBuffer bufferD;

    private final ImInt elements = new ImInt(1_000_000);
    private final ImInt workGroups = new ImInt(1);

    public OpenCLEditor() {
        this.codeEditor = new CodeEditor("Save");
        this.codeEditor.setSaveCallback((source, errorConsumer) -> this.compileProgram(source));
        this.source = """
                void kernel example(global const int* A, global const int* B, global const int* C, global int* D) {
                    int i = get_global_id(0);
                    D[i] = A[i] + B[i] + C[i];
                }
                """;

        this.environment = null;
    }

    private void freeBuffers() {
        if (this.bufferA != null) {
            this.bufferA.free();
            this.bufferA = null;
        }
        if (this.bufferB != null) {
            this.bufferB.free();
            this.bufferB = null;
        }
        if (this.bufferC != null) {
            this.bufferC.free();
            this.bufferC = null;
        }
        if (this.bufferD != null) {
            this.bufferD.free();
            this.bufferD = null;
        }
    }

    private void initBuffers(int itemCount) throws CLException {
        if (this.bufferA == null) {
            this.bufferA = this.kernel.createBuffer(CL_MEM_READ_ONLY, (long) itemCount * Integer.BYTES);
        }
        if (this.bufferB == null) {
            this.bufferB = this.kernel.createBuffer(CL_MEM_READ_ONLY, (long) itemCount * Integer.BYTES);
        }
        if (this.bufferC == null) {
            this.bufferC = this.kernel.createBuffer(CL_MEM_READ_ONLY, (long) itemCount * Integer.BYTES);
        }
        if (this.bufferD == null) {
            this.bufferD = this.kernel.createBuffer(CL_MEM_WRITE_ONLY, (long) itemCount * Integer.BYTES);
        }
    }

    private void selectDevice(VeilOpenCL.DeviceInfo deviceInfo) {
        if (this.environment != null && Objects.equals(this.environment.getDevice(), deviceInfo)) {
            return;
        }

        if (!deviceInfo.compilerAvailable()) {
            return;
        }

        CLEnvironment environment = VeilOpenCL.get().getEnvironment();
        if (environment == null) {
            LOGGER.error("Failed to change CL device");
            return;
        }

        this.free();
        this.environment = environment;
    }

    private void compileProgram(String source) {
        if (this.environment == null) {
            return;
        }

        ResourceLocation name = new ResourceLocation("test");
        this.environment.loadProgram(name, source);
        this.kernel = this.environment.getKernel(name, "example");
        if (this.kernel == null) {
            LOGGER.error("Failed to compile program");
            return;
        }

        this.source = source;
    }

    @Override
    protected void renderComponents() {
        VeilOpenCL.PlatformInfo[] platforms = VeilOpenCL.get().getPlatforms();
        if (platforms.length == 0) {
            ImGui.text("No platforms found");
            return;
        }

        VeilOpenCL.DeviceInfo deviceInfo = this.environment != null ? this.environment.getDevice() : null;
        if (ImGui.beginCombo("Device", deviceInfo == null ? "No Device Selected" : deviceInfo.name())) {
            for (VeilOpenCL.PlatformInfo platform : platforms) {
                for (VeilOpenCL.DeviceInfo device : platform.devices()) {
                    if (device.compilerAvailable() && ImGui.selectable(platform.vendor() + " " + device.name(), deviceInfo == device)) {
                        this.selectDevice(device);
                    }
                }
            }
            ImGui.endCombo();
        }

        ImGui.beginDisabled(deviceInfo == null);
        if (ImGui.button("Edit Source")) {
            this.codeEditor.show(this.source);
        }
        ImGui.endDisabled();

        ImGui.beginDisabled(this.kernel == null);
        ImGui.sameLine();
        if (ImGui.button("Run")) {
            Stopwatch upload = Stopwatch.createStarted();
            int itemCount = this.elements.get();
            IntBuffer A = MemoryUtil.memAllocInt(itemCount);
            IntBuffer B = MemoryUtil.memAllocInt(itemCount);
            IntBuffer C = MemoryUtil.memAllocInt(itemCount);
            for (int i = 0; i < itemCount; i++) {
                A.put(i, i);
                B.put(i, itemCount - i);
                C.put(i, 2 * i);
            }
            upload.stop();

            Stopwatch execute = Stopwatch.createStarted();
            try {
                this.initBuffers(itemCount);

                this.bufferA.writeAsync(A, null);
                this.bufferB.writeAsync(B, null);
                this.bufferC.writeAsync(C, null);

                this.kernel.setPointers(0, this.bufferA);
                this.kernel.setPointers(1, this.bufferB);
                this.kernel.setPointers(2, this.bufferC);
                this.kernel.setPointers(3, this.bufferD);

                this.kernel.execute(itemCount, 1);

                IntBuffer D = MemoryUtil.memAllocInt(itemCount);
                try {
                    this.bufferD.readAsync(D, null);

                    this.environment.finish();

                    execute.stop();
                    System.out.printf("Done (%s upload, %s execute, %sns/item)\n", upload, execute, execute.elapsed(TimeUnit.NANOSECONDS) / itemCount);
                } finally {
                    MemoryUtil.memFree(D);
                }
            } catch (Throwable t) {
                LOGGER.error("Failed to run OpenCL", t);
            }
        }
        ImGui.endDisabled();

        if (ImGui.dragScalar("Elements", ImGuiDataType.U32, this.elements, 1_000, 0, 100_000_000)) {
            this.freeBuffers();
        }

        int max = this.kernel != null ? this.kernel.getMaxWorkGroupSize() : Integer.MAX_VALUE;
        ImGui.beginDisabled(this.kernel == null);
        ImGui.sliderScalar("Local Work Groups", ImGuiDataType.U32, this.workGroups, 1, max);
        ImGui.endDisabled();
    }

    @Override
    public void free() {
        super.free();
        this.freeBuffers();
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
