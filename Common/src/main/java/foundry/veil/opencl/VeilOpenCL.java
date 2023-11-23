package foundry.veil.opencl;

import foundry.veil.Veil;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.opencl.CL11.CL_DEVICE_OPENCL_C_VERSION;
import static org.lwjgl.opencl.KHRICD.CL_PLATFORM_ICD_SUFFIX_KHR;

/**
 * Veil implementation of OpenCL.
 *
 * @author Ocelot
 */
public final class VeilOpenCL implements NativeResource {

    // Prefer platforms that support GPU devices
    private static final Comparator<DeviceInfo> COMPUTE_ORDER = (p1, p2) -> {
        boolean gpu1 = p1.isGpu();
        boolean gpu2 = p2.isGpu();
        return gpu1 == gpu2 ? 0 : (gpu1 ? -1 : 1);
    };

    private static VeilOpenCL instance;

    private final Map<DeviceInfo, OpenCLEnvironment> environments;
    private final Set<DeviceInfo> invalidPlatforms;
    private final PlatformInfo[] platforms;
    private final List<DeviceInfo> priorityDevices;

    private VeilOpenCL() {
        this.environments = new Object2ObjectArrayMap<>();
        this.invalidPlatforms = new HashSet<>();
        this.platforms = VeilOpenCL.requestPlatforms();

        List<DeviceInfo> priorityDevices = new ArrayList<>();
        for (PlatformInfo platform : this.platforms) {
            priorityDevices.addAll(Arrays.asList(platform.devices()));
        }
        priorityDevices.sort(COMPUTE_ORDER);

        this.priorityDevices = Collections.unmodifiableList(priorityDevices);
    }

    /**
     * @return The default OpenCL environment
     */
    public @Nullable OpenCLEnvironment getEnvironment() {
        return this.getEnvironment(OpenCLEnvironmentOptions.DEFAULT);
    }

    /**
     * Retrieves an environment that follows the specified requirements.
     *
     * @param options The requirements for the requested environment
     * @return The environment for a device with those properties or <code>null</code> if no device was found
     */
    public @Nullable OpenCLEnvironment getEnvironment(OpenCLEnvironmentOptions options) {
        for (DeviceInfo deviceInfo : this.getPriorityDevices()) {
            if (options.testDevice(deviceInfo)) {
                return this.getEnvironment(deviceInfo);
            }
        }
        return null;
    }

    /**
     * Retrieves the environment for the specified device.
     *
     * @param deviceInfo The device to retrieve the environment for
     * @return The environment for that device
     */
    public @Nullable OpenCLEnvironment getEnvironment(DeviceInfo deviceInfo) {
        if (this.invalidPlatforms.contains(deviceInfo)) {
            return null;
        }

        OpenCLEnvironment environment = this.environments.get(deviceInfo);
        if (environment == null) {
            try {
                environment = new OpenCLEnvironment(deviceInfo);
                this.environments.put(deviceInfo, environment);
            } catch (OpenCLException e) {
                Veil.LOGGER.error("Failed to create environment for device: " + deviceInfo.name(), e);
                return null;
            }
        }
        return environment;
    }

    /**
     * @return All platforms OpenCL can be run on
     */
    public PlatformInfo[] getPlatforms() {
        return this.platforms;
    }

    /**
     * @return All devices for all platforms sorted by compute priority
     */
    public List<DeviceInfo> getPriorityDevices() {
        return this.priorityDevices;
    }

    @Override
    public void free() {
        this.environments.values().forEach(OpenCLEnvironment::free);
        this.environments.clear();
    }

    /**
     * @return The static Veil OpenCL implementation
     */
    public static VeilOpenCL get() {
        if (instance == null) {
            instance = new VeilOpenCL();
        }
        return instance;
    }

    public static void checkCLError(IntBuffer errcode) throws OpenCLException {
        checkCLError(errcode.get(0));
    }

    public static void checkCLError(int errcode) throws OpenCLException {
        if (errcode != CL_SUCCESS) {
            throw new OpenCLException(errcode);
        }
    }

    private static PlatformInfo[] requestPlatforms() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer num_platforms = stack.mallocInt(1);
            checkCLError(clGetPlatformIDs(null, num_platforms));

            if (num_platforms.get(0) == 0) {
                return new PlatformInfo[0];
            }

            PointerBuffer platforms = stack.mallocPointer(num_platforms.get(0));
            checkCLError(clGetPlatformIDs(platforms, (IntBuffer) null));

            PlatformInfo[] platformInfos = new PlatformInfo[platforms.capacity()];
            for (int i = 0; i < platformInfos.length; i++) {
                platformInfos[i] = PlatformInfo.create(platforms.get(i), stack);
            }

            return platformInfos;
        } catch (OpenCLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProgramBuildInfo(long program, long device, int param) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer log_size = stack.mallocPointer(1);
            VeilOpenCL.checkCLError(clGetProgramBuildInfo(program, device, param, (PointerBuffer) null, log_size));

            ByteBuffer log_data = stack.malloc((int) log_size.get(0));
            VeilOpenCL.checkCLError(clGetProgramBuildInfo(program, device, param, log_data, null));

            return MemoryUtil.memASCII(log_data);
        }
    }

    public static String getPlatformInfoStringUTF8(long platform, int param) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);
            checkCLError(clGetPlatformInfo(platform, param, (ByteBuffer) null, pp));
            int bytes = (int) pp.get(0);

            ByteBuffer buffer = stack.malloc(bytes);
            checkCLError(clGetPlatformInfo(platform, param, buffer, null));

            return MemoryUtil.memUTF8(buffer, bytes - 1);
        }
    }

    public static String getDeviceInfoStringUTF8(long platform, int param) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);
            checkCLError(clGetDeviceInfo(platform, param, (ByteBuffer) null, pp));
            int bytes = (int) pp.get(0);

            ByteBuffer buffer = stack.malloc(bytes);
            checkCLError(clGetDeviceInfo(platform, param, buffer, null));

            return MemoryUtil.memUTF8(buffer, bytes - 1);
        }
    }

    public static int getDeviceInfoInt(long device, int param) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pl = stack.mallocInt(1);
            checkCLError(clGetDeviceInfo(device, param, pl, null));
            return pl.get(0);
        }
    }

    public static long getDeviceInfoLong(long device, int param) throws OpenCLException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pl = stack.mallocLong(1);
            checkCLError(clGetDeviceInfo(device, param, pl, null));
            return pl.get(0);
        }
    }

    /**
     * Information about a platform that can run OpenCL.
     *
     * @param id           The id of this platform
     * @param capabilities The capabilities supported on this platform
     * @param profile      The profile name supported by the implementation
     * @param version      OpenCL version string
     * @param name         Platform name string
     * @param vendor       Platform vendor string
     * @param devices      The list of devices available on this platform
     * @author Ocelot
     */
    public record PlatformInfo(long id,
                               CLCapabilities capabilities,
                               String profile,
                               String version,
                               String name,
                               String vendor,
                               DeviceInfo[] devices) {

        public static PlatformInfo create(long platform, MemoryStack stack) throws OpenCLException {
            CLCapabilities caps = CL.createPlatformCapabilities(platform);
            String profile = getPlatformInfoStringUTF8(platform, CL_PLATFORM_PROFILE);
            String version = getPlatformInfoStringUTF8(platform, CL_PLATFORM_VERSION);
            String name = getPlatformInfoStringUTF8(platform, CL_PLATFORM_NAME);
            String vendor = getPlatformInfoStringUTF8(platform, caps.cl_khr_icd ? CL_PLATFORM_ICD_SUFFIX_KHR : CL_PLATFORM_VENDOR);

            IntBuffer num_devices = stack.mallocInt(1);
            checkCLError(clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, null, num_devices));
            DeviceInfo[] deviceInfos = new DeviceInfo[num_devices.get(0)];

            if (deviceInfos.length > 0) {
                PointerBuffer devices = stack.mallocPointer(deviceInfos.length);
                checkCLError(clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, devices, (IntBuffer) null));

                for (int i = 0; i < deviceInfos.length; i++) {
                    deviceInfos[i] = DeviceInfo.create(devices.get(i), caps);
                }
            }

            return new PlatformInfo(platform, caps, profile, version, name, vendor, deviceInfos);
        }
    }

    /**
     * Information about a device on an OpenCL platform.
     *
     * @param platform              The platform this device is on
     * @param id                    The id of this device
     * @param capabilities          The capabilities of this device
     * @param type                  The OpenCL device type
     * @param vendorId              A unique device vendor identifier
     * @param maxComputeUnits       The number of parallel compute units on the OpenCL device. A work-group executes on a single compute unit. The minimum value is 1
     * @param maxWorkItemDimensions Maximum dimensions that specify the global and local work-item IDs used by the data parallel execution model. (Refer to {@link CL10#clEnqueueNDRangeKernel(long, long, int, PointerBuffer, PointerBuffer, PointerBuffer, PointerBuffer, PointerBuffer) clEnqueueNDRangeKernel})
     * @param maxWorkGroupSize      Maximum number of work-items in a work-group that a device is capable of executing on a single compute unit, for any given kernel-instance running on the device
     * @param maxMemAllocSize       Max size of memory object allocation in bytes
     * @param maxClockFrequency     Maximum configured clock frequency of the device in MHz
     * @param addressBits           The default compute device address space size of the global address space specified as an unsigned integer value in bits. Currently supported values are 32 or 64 bits
     * @param available             If this device is able to execute commands sent to it
     * @param compilerAvailable     If this device has a compiler available to compile program source
     * @param name                  Device name string
     * @param vendor                Vendor name string
     * @param driverVersion         OpenCL software driver version string. Follows a vendor-specific format
     * @param profile               The profile name supported by the device
     * @param version               The OpenCL version supported by the device
     * @param openclCVersion        OpenCL C version string or <code>null</code> if not supported
     * @author Ocelot
     */
    public record DeviceInfo(long platform,
                             long id,
                             CLCapabilities capabilities,
                             long type,
                             int vendorId,
                             int maxComputeUnits,
                             int maxWorkItemDimensions,
                             long maxWorkGroupSize,
                             long maxMemAllocSize,
                             int maxClockFrequency,
                             int addressBits,
                             boolean available,
                             boolean compilerAvailable,
                             String name,
                             String vendor,
                             String driverVersion,
                             String profile,
                             String version,
                             @Nullable String openclCVersion
    ) {

        public static DeviceInfo create(long device, CLCapabilities platformCapabilities) throws OpenCLException {
            CLCapabilities caps = CL.createDeviceCapabilities(device, platformCapabilities);
            long platform = getDeviceInfoLong(device, CL_DEVICE_PLATFORM);
            long deviceType = getDeviceInfoLong(device, CL_DEVICE_TYPE);
            int vendorId = getDeviceInfoInt(device, CL_DEVICE_VENDOR_ID);
            int maxComputeUnits = getDeviceInfoInt(device, CL_DEVICE_MAX_COMPUTE_UNITS);
            int maxWorkItemDimensions = getDeviceInfoInt(device, CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS);
            long maxWorkGroupSize = getDeviceInfoLong(device, CL_DEVICE_MAX_WORK_GROUP_SIZE);
            long maxMemAllocSize = getDeviceInfoLong(device, CL_DEVICE_MAX_MEM_ALLOC_SIZE);
            int maxClockFrequency = getDeviceInfoInt(device, CL_DEVICE_MAX_CLOCK_FREQUENCY);
            int addressBits = getDeviceInfoInt(device, CL_DEVICE_ADDRESS_BITS);
            boolean available = getDeviceInfoInt(device, CL_DEVICE_AVAILABLE) == CL_TRUE;
            boolean compilerAvailable = getDeviceInfoInt(device, CL_DEVICE_COMPILER_AVAILABLE) == CL_TRUE;
            String name = getDeviceInfoStringUTF8(device, CL_DEVICE_NAME);
            String vendor = getDeviceInfoStringUTF8(device, CL_DEVICE_VENDOR);
            String driverVersion = getDeviceInfoStringUTF8(device, CL_DRIVER_VERSION);
            String profile = getDeviceInfoStringUTF8(device, CL_DEVICE_PROFILE);
            String version = getDeviceInfoStringUTF8(device, CL_DEVICE_VERSION);
            String openclCVersion = caps.OpenCL11 ? getDeviceInfoStringUTF8(device, CL_DEVICE_OPENCL_C_VERSION) : null;
            return new DeviceInfo(platform, device, caps, deviceType, vendorId, maxComputeUnits, maxWorkItemDimensions, maxWorkGroupSize, maxMemAllocSize, maxClockFrequency, addressBits, available, compilerAvailable, name, vendor, driverVersion, profile, version, openclCVersion);
        }

        /**
         * An OpenCL device that is the host processor. The
         * host processor runs the OpenCL implementations
         * and is a single or multi-core CPU
         */
        public boolean isCpu() {
            return (this.type & CL_DEVICE_TYPE_CPU) > 0;
        }

        /**
         * An OpenCL device that is a GPU. By this we mean
         * that the device can also be used to accelerate a 3D
         * API such as OpenGL or DirectX
         */
        public boolean isGpu() {
            return (this.type & CL_DEVICE_TYPE_GPU) > 0;
        }

        /**
         * Dedicated OpenCL accelerators (for example the
         * IBM CELL Blade). These devices communicate
         * with the host processor using a peripheral
         * interconnect such as PCIe.
         */
        public boolean isAccelerator() {
            return (this.type & CL_DEVICE_TYPE_ACCELERATOR) > 0;
        }

        /**
         * The default OpenCL device in the system. The
         * default device cannot be a
         * CL_DEVICE_TYPE_CUSTOM device.
         */
        public boolean isDefault() {
            return (this.type & CL_DEVICE_TYPE_DEFAULT) > 0;
        }
    }
}
