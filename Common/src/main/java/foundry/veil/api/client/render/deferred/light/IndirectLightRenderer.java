package foundry.veil.api.client.render.deferred.light;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import foundry.veil.Veil;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.opencl.*;
import foundry.veil.ext.VertexBufferExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Set;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL40C.GL_DRAW_INDIRECT_BUFFER;

/**
 * Draws lights as indirect instanced quads in the scene.
 *
 * @param <T> The type of lights to render
 * @author Ocelot
 */
public abstract class IndirectLightRenderer<T extends Light & IndirectLight<T>> implements LightTypeRenderer<T> {

    private static final CLEnvironment ENVIRONMENT = VeilOpenCL.get().getEnvironment(CLEnvironmentOptions.builder().setRequireGL(true).build());
    private static final int MIN_LIGHTS = 20;

    protected final int lightSize;
    protected final int highResSize;
    protected final int lowResSize;
    protected int maxLights;

    private final VertexBuffer vbo;
    private final int instancedVbo;
    private final int indirectVbo;

    private int visibleLights;
    private CLKernel kernel;
    private CLBuffer clCounter;
    private CLBuffer clFrustumPlanes;
    private CLBuffer clInstancedBuffer;
    private CLBuffer clIndirectBuffer;

    /**
     * Creates a new instanced light renderer with a resizeable light buffer.
     *
     * @param lightSize  The size of each light in bytes
     * @param lowResSize The size of the low-resolution mesh or <code>0</code> to only use the high-detail mesh
     */
    public IndirectLightRenderer(int lightSize, int lowResSize, int positionOffset, int rangeOffset) {
        this.lightSize = lightSize;
        this.maxLights = MIN_LIGHTS;
        this.vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.instancedVbo = glGenBuffers();
        this.indirectVbo = glGenBuffers();

        this.vbo.bind();
        this.vbo.upload(this.createMesh());

        VertexBufferExtension ext = (VertexBufferExtension) this.vbo;
        this.highResSize = ext.veil$getIndexCount() - lowResSize;
        this.lowResSize = lowResSize;

        if (ENVIRONMENT != null) { // FIXME this doesn't work properly
            ResourceLocation name = Veil.veilPath("indirect_light");
            ENVIRONMENT.loadProgram(name, """
                    #define HIGH_RES_SIZE %d
                    #define LOW_RES_SIZE %d
                    #define LIGHT_SIZE %d
                    #define POSITION_OFFSET %d
                    #define RANGE_OFFSET %d
                                
                    bool testSphere(global const float* FrustumPlanes, const float x, const float y, const float z, const float r) {
                        return FrustumPlanes[0] * x + FrustumPlanes[1] * y + FrustumPlanes[2] * z + FrustumPlanes[3] >= -r &&
                               FrustumPlanes[4] * x + FrustumPlanes[5] * y + FrustumPlanes[6] * z + FrustumPlanes[7] >= -r &&
                               FrustumPlanes[8] * x + FrustumPlanes[9] * y + FrustumPlanes[10] * z + FrustumPlanes[11] >= -r &&
                               FrustumPlanes[12] * x + FrustumPlanes[13] * y + FrustumPlanes[14] * z + FrustumPlanes[15] >= -r &&
                               FrustumPlanes[16] * x + FrustumPlanes[17] * y + FrustumPlanes[18] * z + FrustumPlanes[19] >= -r &&
                               FrustumPlanes[20] * x + FrustumPlanes[21] * y + FrustumPlanes[22] * z + FrustumPlanes[23] >= -r;
                    }
                                   
                    void kernel update_draw(const float4 CameraPos, volatile global int* Counter, global const float* FrustumPlanes, global const float* LightData, global uint* DrawData) {
                        const int lightId = get_global_id(0);
                        const int lightDataIndex = lightId * LIGHT_SIZE;
                        float x = LightData[lightDataIndex + POSITION_OFFSET];
                        float y = LightData[lightDataIndex + POSITION_OFFSET + 1];
                        float z = LightData[lightDataIndex + POSITION_OFFSET + 2];
                        float range = LightData[lightDataIndex + RANGE_OFFSET];
                        
                        bool visible = testSphere(FrustumPlanes, x - CameraPos.x, y - CameraPos.y, z - CameraPos.z, range * 1.414);
                        if (visible) {
                            int i = atomic_inc(Counter) * 5;
                            bool highRes = (x - CameraPos.x) * (x - CameraPos.x) + (y - CameraPos.y) * (y - CameraPos.y) + (z - CameraPos.z) * (z - CameraPos.z) <= range * range;
                            DrawData[i] = highRes ? HIGH_RES_SIZE : LOW_RES_SIZE;
                            DrawData[i + 1] = 1;
                            DrawData[i + 2] = !highRes ? HIGH_RES_SIZE : 0;
                            DrawData[i + 3] = 0;
                            DrawData[i + 4] = lightId;
                        }
                    }
                    """.formatted(this.highResSize, this.lowResSize, lightSize, positionOffset, rangeOffset));

            try {
                this.kernel = ENVIRONMENT.createKernel(name, "update_draw");
                this.clCounter = this.kernel.createBuffer(CL_MEM_READ_WRITE, Integer.BYTES);
                this.clFrustumPlanes = this.kernel.createBuffer(CL_MEM_READ_ONLY, 24 * Float.BYTES);
                this.kernel.setPointers(1, this.clCounter);
                this.kernel.setPointers(2, this.clFrustumPlanes);
            } catch (Exception e) {
                Veil.LOGGER.error("Failed to create indirect kernel", e);
                this.freeCL();
            }
        }

        // Initialize data buffers

        glBindBuffer(GL_ARRAY_BUFFER, this.instancedVbo);
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, this.indirectVbo);

        this.initBuffers();

        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);
        this.setupBufferState(); // Only set up state for instanced buffer
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        VertexBuffer.unbind();
    }

    /**
     * @return The mesh data each instanced light will be rendered with use
     */
    protected abstract BufferBuilder.RenderedBuffer createMesh();

    /**
     * Sets up the instanced buffer state.
     */
    protected abstract void setupBufferState();

    /**
     * Sets up the render state for drawing all lights.
     *
     * @param lightRenderer The renderer instance
     * @param lights        All lights in the order they are in the instanced buffer
     */
    protected abstract void setupRenderState(LightRenderer lightRenderer, List<T> lights);

    /**
     * Clears the render state after drawing all lights.
     *
     * @param lightRenderer The renderer instance
     * @param lights        All lights in the order they are in the instanced buffer
     */
    protected abstract void clearRenderState(LightRenderer lightRenderer, List<T> lights);

    private void initBuffers() {
        glBufferData(GL_ARRAY_BUFFER, (long) this.maxLights * this.lightSize, GL_DYNAMIC_DRAW);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, (long) this.maxLights * Integer.BYTES * 5, GL_DYNAMIC_DRAW);

        if (this.kernel != null) {
            try {
                if (this.clInstancedBuffer != null) {
                    this.clInstancedBuffer.free();
                }
                if (this.clIndirectBuffer != null) {
                    this.clIndirectBuffer.free();
                }
                this.clInstancedBuffer = this.kernel.createBufferFromGL(CL_MEM_READ_ONLY, this.instancedVbo);
                this.clIndirectBuffer = this.kernel.createBufferFromGL(CL_MEM_WRITE_ONLY, this.indirectVbo);
                this.kernel.setPointers(3, this.clInstancedBuffer);
                this.kernel.setPointers(4, this.clIndirectBuffer);
            } catch (CLException e) {
                Veil.LOGGER.error("Failed to initialize indirect compute", e);
                this.freeCL();
            }
        } else {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer buffer = stack.calloc(Integer.BYTES * 5);
                buffer.putInt(0, ((VertexBufferExtension) this.vbo).veil$getIndexCount());
                buffer.putInt(4, 1);

                for (int i = 0; i < this.maxLights; i++) {
                    glBufferSubData(GL_DRAW_INDIRECT_BUFFER, i * Integer.BYTES * 5L, buffer);
                }
            }
        }
    }

    private boolean shouldDrawHighResolution(T light, CullFrustum frustum) {
        float radius = light.getRadius();
        return frustum.getPosition().distanceSquared(light.getPosition()) <= radius * radius;
    }

    private boolean isVisible(T light, CullFrustum frustum) {
        Vector3d position = light.getPosition();
        float radius = light.getRadius();
        return frustum.testSphere(position.x, position.y, position.z, radius * 1.414F);
    }

    private void updateAllLights(List<T> lights) {
        ByteBuffer dataBuffer = MemoryUtil.memAlloc(lights.size() * this.lightSize);
        int pointer = 0;
        for (T light : lights) {
            light.clean();
            dataBuffer.position((pointer++) * this.lightSize);
            light.store(dataBuffer);
        }

        if (pointer > 0) {
            dataBuffer.rewind();
            glBufferSubData(GL_ARRAY_BUFFER, 0, dataBuffer);
        }
        MemoryUtil.memFree(dataBuffer);
    }

    private int updateVisibility(List<T> lights, CullFrustum frustum) {
        if (this.kernel != null) {
            ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
            try (MemoryStack stack = MemoryStack.stackPush()) {
                profiler.push("acquire");
                this.kernel.acquireFromGL(this.clInstancedBuffer, this.clIndirectBuffer);
                profiler.popPush("upload");

                this.clCounter.writeAsync(0L, stack.ints(0), null);

                ByteBuffer planes = stack.malloc(6 * 4 * Float.BYTES);
                int index = 0;
                for (Vector4fc plane : frustum.getPlanes()) {
                    plane.get(index, planes);
                    index += 4 * Float.BYTES;
                }
                this.clFrustumPlanes.writeAsync(0L, planes, null);

                profiler.popPush("setup");
                Vector3dc pos = frustum.getPosition();
                this.kernel.setVector4f(0, (float) pos.x(), (float) pos.y(), (float) pos.z(), 0);
                this.kernel.execute(lights.size(), 1);

                profiler.popPush("release");
                this.kernel.releaseToGL(this.clInstancedBuffer, this.clIndirectBuffer);

                profiler.popPush("read");
                IntBuffer data = stack.mallocInt(1);
                this.clCounter.read(0L, data);
                profiler.pop();
                return data.get(0);
            } catch (CLException e) {
                Veil.LOGGER.error("Failed to run indirect compute", e);
                this.freeCL();
            }
        }

        int count = 0;
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, this.indirectVbo);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(this.lowResSize > 0 ? Integer.BYTES * 5 : Integer.BYTES);

            int index = 0;
            for (T light : lights) {
                if (this.isVisible(light, frustum)) {
                    if (this.lowResSize > 0) {
                        boolean highRes = this.shouldDrawHighResolution(light, frustum);
                        buffer.putInt(0, highRes ? this.highResSize : this.lowResSize);
                        buffer.putInt(4, 1);
                        buffer.putInt(8, !highRes ? this.highResSize : 0);
                        buffer.putInt(12, 0);
                        buffer.putInt(16, index);
                        glBufferSubData(GL_DRAW_INDIRECT_BUFFER, count * Integer.BYTES * 5L, buffer);
                    } else {
                        buffer.putInt(0, index);
                        glBufferSubData(GL_DRAW_INDIRECT_BUFFER, count * Integer.BYTES * 5L + 16, buffer);
                    }
                    count++;
                }
                index++;
            }
        }
        return count;
    }

    @Override
    public void prepareLights(LightRenderer lightRenderer, List<T> lights, Set<T> removedLights, CullFrustum frustum) {
        glBindBuffer(GL_ARRAY_BUFFER, this.instancedVbo);

        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("resize");

        // If there is no space, then resize
        boolean rebuild = false;
        if (lights.size() > this.maxLights) {
            rebuild = true;
            this.maxLights = (int) Math.max(Math.max(Math.ceil(this.maxLights / 2.0), MIN_LIGHTS), lights.size() * 1.5);
            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, this.indirectVbo);
            this.initBuffers();
            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);
        }
        profiler.popPush("update");

        // The instanced buffer needs to be updated
        if (rebuild || !removedLights.isEmpty()) {
            this.updateAllLights(lights);
        } else {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer buffer = stack.malloc(this.lightSize);
                for (int i = 0; i < lights.size(); i++) {
                    T light = lights.get(i);
                    if (light.isDirty()) {
                        light.clean();
                        light.store(buffer);
                        buffer.rewind();
                        glBufferSubData(GL_ARRAY_BUFFER, (long) i * this.lightSize, buffer);
                    }
                }
            }
        }

        profiler.popPush("visibility");

        // Fill indirect buffer draw calls
        this.visibleLights = !lights.isEmpty() ? this.updateVisibility(lights, frustum) : 0;

        profiler.pop();

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void renderLights(LightRenderer lightRenderer, List<T> lights) {
        this.vbo.bind();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, this.indirectVbo);

        if (this.visibleLights > 0) {
            this.setupRenderState(lightRenderer, lights);
            lightRenderer.applyShader();
            ((VertexBufferExtension) this.vbo).veil$drawIndirect(0L, this.visibleLights, 0);
            this.clearRenderState(lightRenderer, lights);
        }

        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);
        VertexBuffer.unbind();
    }

    private void freeCL() {
        if (this.kernel != null) {
            this.kernel.free();
            this.kernel = null;
        }
        this.clCounter = null;
        this.clFrustumPlanes = null;
        this.clInstancedBuffer = null;
        this.clIndirectBuffer = null;
    }

    @Override
    public int getVisibleLights() {
        return this.visibleLights;
    }

    @Override
    public void free() {
        this.vbo.close();
        glDeleteBuffers(this.instancedVbo);
        glDeleteBuffers(this.indirectVbo);
        this.freeCL();
    }
}
