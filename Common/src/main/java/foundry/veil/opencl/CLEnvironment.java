package foundry.veil.opencl;

import com.mojang.logging.LogUtils;
import foundry.veil.opencl.event.CLEventDispatcher;
import foundry.veil.opencl.event.CLLegacyEventDispatcher;
import foundry.veil.opencl.event.CLNativeEventDispatcher;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.opencl.CL20.clCreateCommandQueueWithProperties;

/**
 * An OpenCL runtime environment on a specific device.
 *
 * @author Ocelot
 */
public class CLEnvironment implements NativeResource {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter SHADERS = new FileToIdConverter("pinwheel/compute", ".cl");

    private final VeilOpenCL.DeviceInfo device;
    private final CLContextCallback errorCallback;
    private final long context;
    private final long commandQueue;
    private final Map<ResourceLocation, ProgramData> programs;
    private final CLEventDispatcher eventDispatcher;

    public CLEnvironment(VeilOpenCL.DeviceInfo deviceInfo) throws CLException {
        this.device = deviceInfo;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer ctxProps = stack.mallocPointer(3);
            ctxProps
                    .put(0, CL_CONTEXT_PLATFORM)
                    .put(1, deviceInfo.platform())
                    .put(2, 0);

            long device = deviceInfo.id();

            this.errorCallback = CLContextCallback.create((errinfo, private_info, cb, user_data) -> {
                VeilOpenCL.LOGGER.error("[LWJGL] cl_context_callback");
                VeilOpenCL.LOGGER.error("\tInfo: " + MemoryUtil.memUTF8(errinfo));
            });
            IntBuffer errcode_ret = stack.callocInt(1);

            try {
                this.context = clCreateContext(ctxProps, device, this.errorCallback, MemoryUtil.NULL, errcode_ret);
                VeilOpenCL.checkCLError(errcode_ret);

                this.commandQueue = clCreateCommandQueueWithProperties(this.context, device, null, errcode_ret);
                VeilOpenCL.checkCLError(errcode_ret);

                if (this.commandQueue == MemoryUtil.NULL) {
                    throw new IllegalStateException("Failed to create OpenCL queue");
                }
            } catch (Exception e) {
                this.free();
                throw e;
            }
        }

        this.programs = new HashMap<>();
        this.eventDispatcher = deviceInfo.capabilities().clSetEventCallback != 0 ? new CLNativeEventDispatcher() : new CLLegacyEventDispatcher();
    }

    /**
     * Loads the specified source code under the specified name.
     *
     * @param name   The name of the shader to load
     * @param source The source code to compile
     */
    public void loadProgram(ResourceLocation name, CharSequence source) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long program = 0;
            IntBuffer errcode_ret = stack.callocInt(1);
            try {
                program = clCreateProgramWithSource(this.context, source, errcode_ret);
                VeilOpenCL.checkCLError(errcode_ret);

                long device = this.device.id();
                int programStatus = clBuildProgram(program, device, "", null, 0);
                if (programStatus != CL_SUCCESS) {
                    System.err.println(VeilOpenCL.getProgramBuildInfo(program, device, CL_PROGRAM_BUILD_LOG));
                    throw new CLException("Failed to compile program", programStatus);
                }

                ProgramData oldProgram = this.programs.put(name, new ProgramData(program));
                if (oldProgram != null) {
                    LOGGER.info("Deleting old program: {}", name);
                    oldProgram.free();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load program from source: {}", name, e);
                if (program != 0) {
                    clReleaseProgram(program);
                }
            }
        }
    }

    /**
     * Loads the specified program binary from file.
     *
     * @param provider The provider for files
     * @param name     The name of the shader file
     * @throws IOException If any errors occurs
     */
    public void loadProgram(ResourceLocation name, ResourceProvider provider) throws IOException {
        Resource resource = provider.getResourceOrThrow(SHADERS.idToFile(name));
        try (InputStream stream = resource.open()) {
            this.loadProgram(name, IOUtils.toString(stream, StandardCharsets.UTF_8));
        }
    }

    /**
     * <p>Creates a kernel for the specified shader program.</p>
     * <p>The returned kernel should be freed when it is no longer needed. To be <a href="https://tenor.com/view/let-me-be-clear-uhhh-meme-gif-25693361">clear</a>, after the last kernel for a program has been freed the program WILL be freed and must be loaded again.</p>
     *
     * @param program    The name of the program to get the kernel from
     * @param kernelName The name of the kernel
     * @return The kernel created
     * @throws CLException If there was an error creating the kernel for any reason
     */
    public CLKernel createKernel(ResourceLocation program, String kernelName) throws CLException {
        ProgramData programData = this.programs.get(program);
        if (programData == null) {
            throw new CLException("Unknown program: " + kernelName, CL_INVALID_PROGRAM);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer errcode_ret = stack.callocInt(1);
            long kernelId = clCreateKernel(programData.id, kernelName, errcode_ret);
            if (errcode_ret.get(0) == CL_INVALID_KERNEL_NAME) {
                throw new CLException("Failed to find kernel: " + kernelName, errcode_ret.get(0));
            }
            VeilOpenCL.checkCLError(errcode_ret);

            CLKernel kernel = new CLKernel(this, program, kernelId);
            programData.kernels.add(kernel);
            return kernel;
        }
    }

    /**
     * Blocks until all CL commands have completed.
     *
     * @throws CLException If any error occurs while trying to block
     */
    public void finish() throws CLException {
        VeilOpenCL.checkCLError(clFinish(this.commandQueue));
    }

    /**
     * Destroys all programs.
     */
    public void freePrograms() {
        this.programs.values().forEach(NativeResource::free);
        this.programs.clear();
    }

    @Override
    public void free() {
        try {
            this.finish();
        } catch (CLException ignored) {
        }
        this.errorCallback.free();
        if (this.commandQueue != 0) {
            clReleaseCommandQueue(this.commandQueue);
        }
        if (this.context != 0) {
            clReleaseContext(this.context);
        }
        this.freePrograms();

        try {
            this.eventDispatcher.close();
        } catch (InterruptedException e) {
            LOGGER.error("Failed to stop event dispatcher", e);
        }
    }

    @ApiStatus.Internal
    void free(CLKernel clKernel) {
        ResourceLocation name = clKernel.getProgram();
        ProgramData programData = this.programs.get(name);
        if (programData == null) {
            return;
        }

        programData.kernels.remove(clKernel);
        if (programData.kernels.isEmpty()) {
            LOGGER.info("Deleting kernel program: {}", name);
            programData.free();
            this.programs.remove(name);
        }
    }

    /**
     * @return The device this environment is in
     */
    public VeilOpenCL.DeviceInfo getDevice() {
        return this.device;
    }

    /**
     * @return The dispatcher for event callbacks
     */
    public CLEventDispatcher getEventDispatcher() {
        return this.eventDispatcher;
    }

    /**
     * @return The pointer to the OpenCL context
     */
    public long getContext() {
        return this.context;
    }

    /**
     * @return The pointer to the OpenCL queue
     */
    public long getCommandQueue() {
        return this.commandQueue;
    }

    private record ProgramData(long id, Set<CLKernel> kernels) implements NativeResource {

        private ProgramData(long id) {
            this(id, new HashSet<>());
        }

        @Override
        public void free() {
            clReleaseProgram(this.id);
            this.kernels.clear();
        }
    }
}
