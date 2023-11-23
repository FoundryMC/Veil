import foundry.veil.opencl.CLBuffer;
import foundry.veil.opencl.CLKernel;
import foundry.veil.opencl.CLEnvironment;
import foundry.veil.opencl.VeilOpenCL;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opencl.CL10.CL_MEM_READ_ONLY;
import static org.lwjgl.opencl.CL10.CL_MEM_WRITE_ONLY;

public class CLEventDispatcherTest {

    @Test
    public void testEnvironment() throws Exception {
        try (VeilOpenCL cl = VeilOpenCL.get(); MemoryStack stack = MemoryStack.stackPush()) {
            Assertions.assertNotNull(cl);
            CLEnvironment environment = cl.getEnvironment();
            Assertions.assertNotNull(environment);

            environment.loadProgram(new ResourceLocation("test"), """
                    void kernel test() {
                    }
                    """);

            CLKernel kernel = environment.getKernel(new ResourceLocation("test"), "test");
            Assertions.assertNotNull(kernel);

            CLBuffer buffer = kernel.createBuffer(CL_MEM_READ_ONLY, 64);
            ByteBuffer data = stack.calloc(64);
            data.putInt(0, 1);
            buffer.write(data);

            ByteBuffer read = stack.malloc(data.capacity());
            buffer.read(read);
            Assertions.assertEquals(1, read.getInt(0));
        }
    }

    @Test
    public void testExecute() throws Exception {
        try (VeilOpenCL cl = VeilOpenCL.get()) {
            Assertions.assertNotNull(cl);
            CLEnvironment environment = cl.getEnvironment();
            Assertions.assertNotNull(environment);

            environment.loadProgram(new ResourceLocation("test"), """
                    void kernel test(global const int* a, global int* b) {
                        int index = get_global_id(0);
                        b[index] = a[index];
                    }
                    """);

            CLKernel kernel = environment.getKernel(new ResourceLocation("test"), "test");
            Assertions.assertNotNull(kernel);

            IntBuffer data = MemoryUtil.memAllocInt(1_000_000);
            CLBuffer input = kernel.createBuffer(CL_MEM_READ_ONLY, (long) data.capacity() * Integer.BYTES);
            for (int i = 0; i < data.capacity(); i++) {
                data.put(i, i);
            }
            input.writeAsync(data, () -> System.out.println("Uploaded"));
            System.out.println("Skipped");

            IntBuffer outputData = MemoryUtil.memAllocInt(data.capacity());
            CLBuffer output = kernel.createBuffer(CL_MEM_WRITE_ONLY, (long) outputData.capacity() * Integer.BYTES);

            kernel.setPointers(0, input);
            kernel.setPointers(1, output);
            kernel.execute(data.capacity(), 1);
            output.readAsync(outputData, () -> System.out.println("Read"));
            System.out.println("Skipped");

            environment.finish();

            for (int i = 0; i < outputData.capacity(); i++) {
                Assertions.assertEquals(data.get(i), outputData.get(i));
            }
        }
    }
}
