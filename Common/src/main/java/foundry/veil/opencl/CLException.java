package foundry.veil.opencl;

import java.util.Map;

import static org.lwjgl.opencl.CL10.*;

/**
 * An exception thrown when something in OpenCL goes wrong.
 *
 * @author Ocelot
 */
public class CLException extends Exception {

    private static final Map<Integer, String> ERROR_CODES = Map.ofEntries(
            Map.entry(CL_DEVICE_NOT_FOUND, "CL_DEVICE_NOT_FOUND"),
            Map.entry(CL_DEVICE_NOT_AVAILABLE, "CL_DEVICE_NOT_AVAILABLE"),
            Map.entry(CL_COMPILER_NOT_AVAILABLE, "CL_COMPILER_NOT_AVAILABLE"),
            Map.entry(CL_MEM_OBJECT_ALLOCATION_FAILURE, "CL_MEM_OBJECT_ALLOCATION_FAILURE"),
            Map.entry(CL_OUT_OF_RESOURCES, "CL_OUT_OF_RESOURCES"),
            Map.entry(CL_OUT_OF_HOST_MEMORY, "CL_OUT_OF_HOST_MEMORY"),
            Map.entry(CL_PROFILING_INFO_NOT_AVAILABLE, "CL_PROFILING_INFO_NOT_AVAILABLE"),
            Map.entry(CL_MEM_COPY_OVERLAP, "CL_MEM_COPY_OVERLAP"),
            Map.entry(CL_IMAGE_FORMAT_MISMATCH, "CL_IMAGE_FORMAT_MISMATCH"),
            Map.entry(CL_IMAGE_FORMAT_NOT_SUPPORTED, "CL_IMAGE_FORMAT_NOT_SUPPORTED"),
            Map.entry(CL_BUILD_PROGRAM_FAILURE, "CL_BUILD_PROGRAM_FAILURE"),
            Map.entry(CL_MAP_FAILURE, "CL_MAP_FAILURE"),
            Map.entry(CL_INVALID_VALUE, "CL_INVALID_VALUE"),
            Map.entry(CL_INVALID_DEVICE_TYPE, "CL_INVALID_DEVICE_TYPE"),
            Map.entry(CL_INVALID_PLATFORM, "CL_INVALID_PLATFORM"),
            Map.entry(CL_INVALID_DEVICE, "CL_INVALID_DEVICE"),
            Map.entry(CL_INVALID_CONTEXT, "CL_INVALID_CONTEXT"),
            Map.entry(CL_INVALID_QUEUE_PROPERTIES, "CL_INVALID_QUEUE_PROPERTIES"),
            Map.entry(CL_INVALID_COMMAND_QUEUE, "CL_INVALID_COMMAND_QUEUE"),
            Map.entry(CL_INVALID_HOST_PTR, "CL_INVALID_HOST_PTR"),
            Map.entry(CL_INVALID_MEM_OBJECT, "CL_INVALID_MEM_OBJECT"),
            Map.entry(CL_INVALID_IMAGE_FORMAT_DESCRIPTOR, "CL_INVALID_IMAGE_FORMAT_DESCRIPTOR"),
            Map.entry(CL_INVALID_IMAGE_SIZE, "CL_INVALID_IMAGE_SIZE"),
            Map.entry(CL_INVALID_SAMPLER, "CL_INVALID_SAMPLER"),
            Map.entry(CL_INVALID_BINARY, "CL_INVALID_BINARY"),
            Map.entry(CL_INVALID_BUILD_OPTIONS, "CL_INVALID_BUILD_OPTIONS"),
            Map.entry(CL_INVALID_PROGRAM, "CL_INVALID_PROGRAM"),
            Map.entry(CL_INVALID_PROGRAM_EXECUTABLE, "CL_INVALID_PROGRAM_EXECUTABLE"),
            Map.entry(CL_INVALID_KERNEL_NAME, "CL_INVALID_KERNEL_NAME"),
            Map.entry(CL_INVALID_KERNEL_DEFINITION, "CL_INVALID_KERNEL_DEFINITION"),
            Map.entry(CL_INVALID_KERNEL, "CL_INVALID_KERNEL"),
            Map.entry(CL_INVALID_ARG_INDEX, "CL_INVALID_ARG_INDEX"),
            Map.entry(CL_INVALID_ARG_VALUE, "CL_INVALID_ARG_VALUE"),
            Map.entry(CL_INVALID_ARG_SIZE, "CL_INVALID_ARG_SIZE"),
            Map.entry(CL_INVALID_KERNEL_ARGS, "CL_INVALID_KERNEL_ARGS"),
            Map.entry(CL_INVALID_WORK_DIMENSION, "CL_INVALID_WORK_DIMENSION"),
            Map.entry(CL_INVALID_WORK_GROUP_SIZE, "CL_INVALID_WORK_GROUP_SIZE"),
            Map.entry(CL_INVALID_WORK_ITEM_SIZE, "CL_INVALID_WORK_ITEM_SIZE"),
            Map.entry(CL_INVALID_GLOBAL_OFFSET, "CL_INVALID_GLOBAL_OFFSET"),
            Map.entry(CL_INVALID_EVENT_WAIT_LIST, "CL_INVALID_EVENT_WAIT_LIST"),
            Map.entry(CL_INVALID_EVENT, "CL_INVALID_EVENT"),
            Map.entry(CL_INVALID_OPERATION, "CL_INVALID_OPERATION"),
            Map.entry(CL_INVALID_BUFFER_SIZE, "CL_INVALID_BUFFER_SIZE"),
            Map.entry(CL_INVALID_GLOBAL_WORK_SIZE, "CL_INVALID_GLOBAL_WORK_SIZE"));

    private final int error;

    public CLException(String message, int errCode) {
        super(message + ". " + String.format("OpenCL error: %s", ERROR_CODES.getOrDefault(errCode, Integer.toString(errCode))));
        this.error = errCode;
    }

    public CLException(int errCode) {
        super(String.format("OpenCL error: %s", ERROR_CODES.getOrDefault(errCode, Integer.toString(errCode))));
        this.error = errCode;
    }

    /**
     * @return The error code for this error
     */
    public int getError() {
        return this.error;
    }
}
