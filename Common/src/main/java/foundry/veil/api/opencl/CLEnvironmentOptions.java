package foundry.veil.api.opencl;

import org.lwjgl.opencl.CLCapabilities;

import java.util.function.Function;

import static org.lwjgl.opencl.CL10.*;

/**
 * Defines requirements for an OpenCL environment when requesting one from {@link VeilOpenCL}.
 *
 * @param version         The minimum required version
 * @param deviceMask      The mask for what types of devices should be used
 * @param requireCompiler Whether the device needs to be able to compile program sources
 * @param requireOpenGL   Whether the device needs to be able to support a mixed OpenGL/OpenCL environment
 * @author Ocelot
 */
public record CLEnvironmentOptions(CLVersion version,
                                   int deviceMask,
                                   boolean requireCompiler,
                                   boolean requireOpenGL) {

    /**
     * The default environment options.
     */
    public static final CLEnvironmentOptions DEFAULT = builder().build();

    /**
     * Tests if the specified device follows the required options specified.
     *
     * @param deviceInfo The device to test
     * @return Whether the device has all requirements
     */
    public boolean testDevice(VeilOpenCL.DeviceInfo deviceInfo) {
        if ((deviceInfo.type() & this.deviceMask) == 0) {
            return false;
        }
        if (this.requireCompiler && !deviceInfo.compilerAvailable()) {
            return false;
        }
        if (this.requireOpenGL && !this.version.testGL(deviceInfo.capabilities())) {
            return false;
        }

        return this.version.test(deviceInfo.capabilities());
    }

    /**
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author Ocelot
     */
    public static class Builder {

        private CLVersion clVersion;
        private int deviceMask;
        private boolean requireCompiler;
        private boolean requireOpenGL;

        public Builder() {
            this.clVersion = CLVersion.CL12;
            this.deviceMask = CL_DEVICE_TYPE_ALL;
            this.requireCompiler = true;
            this.requireOpenGL = false;
        }

        /**
         * Sets the version of OpenCL to target.
         *
         * @param clVersion The CL version to use
         */
        public Builder setClVersion(CLVersion clVersion) {
            this.clVersion = clVersion;
            return this;
        }

        /**
         * Sets the target of the environment to the default device.
         */
        public Builder deviceDefault() {
            this.deviceMask = CL_DEVICE_TYPE_DEFAULT;
            return this;
        }

        /**
         * Sets the target of the environment to the GPU.
         */
        public Builder deviceGpu() {
            this.deviceMask = CL_DEVICE_TYPE_GPU;
            return this;
        }

        /**
         * Sets the target of the environment to the CPU.
         */
        public Builder deviceCpu() {
            this.deviceMask = CL_DEVICE_TYPE_CPU;
            return this;
        }

        /**
         * Sets the target of the environment to a compute accelerator.
         */
        public Builder deviceAccelerator() {
            this.deviceMask = CL_DEVICE_TYPE_ACCELERATOR;
            return this;
        }

        /**
         * Sets the devices the environment should target. If unsure, leave the default.
         *
         * @param deviceMask The mask for what kinds of devices to use
         */
        public Builder deviceMask(int deviceMask) {
            this.deviceMask = deviceMask;
            return this;
        }

        /**
         * Sets whether a compiler is required for the device. This is only needed to use string programs.
         *
         * @param requireCompiler Whether the device needs to have a compiler
         */
        public void setRequireCompiler(boolean requireCompiler) {
            this.requireCompiler = requireCompiler;
        }

        /**
         * Sets whether a compiler is required for the device. This is only needed to use string programs.
         *
         * @param requireOpenGL Whether the device needs to have a compiler
         */
        public void setRequireGL(boolean requireOpenGL) {
            this.requireOpenGL = requireOpenGL;
        }

        /**
         * @return A new environment option spec
         */
        public CLEnvironmentOptions build() {
            return new CLEnvironmentOptions(this.clVersion, this.deviceMask, this.requireCompiler, this.requireOpenGL);
        }
    }

    /**
     * Supported versions of OpenCL.
     *
     * @author Ocelot
     */
    public enum CLVersion {
        CL10(caps -> caps.OpenCL10, caps -> caps.OpenCL10GL),
        CL11(caps -> caps.OpenCL11),
        CL12(caps -> caps.OpenCL12, caps -> caps.OpenCL12GL),
        CL20(caps -> caps.OpenCL20),
        CL21(caps -> caps.OpenCL21),
        CL22(caps -> caps.OpenCL22),
        CL30(caps -> caps.OpenCL30);

        private final Function<CLCapabilities, Boolean> version;
        private final Function<CLCapabilities, Boolean> glVersion;

        CLVersion(Function<CLCapabilities, Boolean> version) {
            this(version, version);
        }

        CLVersion(Function<CLCapabilities, Boolean> version, Function<CLCapabilities, Boolean> glVersion) {
            this.version = version;
            this.glVersion = glVersion;
        }

        /**
         * Tests if the specified capabilities support this CL version.
         *
         * @param capabilities The capabilities to check
         * @return Whether this version is supported
         */
        public boolean test(CLCapabilities capabilities) {
            return this.version.apply(capabilities);
        }

        /**
         * Tests if the specified capabilities support this CL GL version.
         *
         * @param capabilities The capabilities to check
         * @return Whether this GL version is supported
         */
        public boolean testGL(CLCapabilities capabilities) {
            return this.glVersion.apply(capabilities);
        }
    }
}
