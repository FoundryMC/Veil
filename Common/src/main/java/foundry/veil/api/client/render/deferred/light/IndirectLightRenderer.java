package foundry.veil.api.client.render.deferred.light;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import foundry.veil.Veil;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.opencl.*;
import foundry.veil.ext.VertexBufferExtension;
import net.minecraft.resources.ResourceLocation;
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

    protected final int lightSize;
    protected final int highResSize;
    protected final int lowResSize;
    protected int maxLights;

    private final VertexBuffer vbo;
    private final int instancedVbo;
    private final int indirectVbo;

    private CLKernel kernel;
    private final CLBuffer clCounter;
    private final CLBuffer clFrustumPlanes;
    private final CLGLBuffer clInstancedBuffer;
    private final CLGLBuffer clIndirectBuffer;

    /**
     * Creates a new instanced light renderer with a resizeable light buffer.
     *
     * @param lightSize  The size of each light in bytes
     * @param lowResSize The size of the low-resolution mesh or <code>0</code> to only use the high-detail mesh
     */
    public IndirectLightRenderer(int lightSize, int lowResSize, int positionOffset, int rangeOffset) {
        this.lightSize = lightSize;
        this.maxLights = 100;
        this.vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.instancedVbo = glGenBuffers();
        this.indirectVbo = glGenBuffers();

        this.vbo.bind();
        this.vbo.upload(this.createMesh());

        VertexBufferExtension ext = (VertexBufferExtension) this.vbo;
        this.highResSize = ext.veil$getIndexCount() - lowResSize;
        this.lowResSize = lowResSize;

        glBindBuffer(GL_ARRAY_BUFFER, this.instancedVbo);
        glBufferData(GL_ARRAY_BUFFER, (long) this.maxLights * this.lightSize, GL_DYNAMIC_DRAW);
        this.setupBufferState();
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, this.indirectVbo);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, (long) this.maxLights * Integer.BYTES * 5, GL_STREAM_DRAW);
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);

        VertexBuffer.unbind();

        CLKernel kernel = null;
        CLBuffer clCounter = null;
        CLBuffer clFrustumPlanes = null;
        CLGLBuffer clInstancedBuffer = null;
        CLGLBuffer clIndirectBuffer = null;
        if (ENVIRONMENT != null && false) { // FIXME this doesn't work properly
            ResourceLocation name = Veil.veilPath("indirect_light");
            ENVIRONMENT.loadProgram(name, """                
                    bool testSphere(global const float* FrustumPlanes, const float x, const float y, const float z, const float r) {
                        float nxX = FrustumPlanes[0];
                        float nxY = FrustumPlanes[1];
                        float nxZ = FrustumPlanes[2];
                        float nxW = FrustumPlanes[3];
                        float pxX = FrustumPlanes[4];
                        float pxY = FrustumPlanes[5];
                        float pxZ = FrustumPlanes[6];
                        float pxW = FrustumPlanes[7];
                        float nyX = FrustumPlanes[8];
                        float nyY = FrustumPlanes[9];
                        float nyZ = FrustumPlanes[10];
                        float nyW = FrustumPlanes[11];
                        float pyX = FrustumPlanes[12];
                        float pyY = FrustumPlanes[13];
                        float pyZ = FrustumPlanes[14];
                        float pyW = FrustumPlanes[15];
                        float nzX = FrustumPlanes[16];
                        float nzY = FrustumPlanes[17];
                        float nzZ = FrustumPlanes[18];
                        float nzW = FrustumPlanes[19];
                        float pzX = FrustumPlanes[20];
                        float pzY = FrustumPlanes[21];
                        float pzZ = FrustumPlanes[22];
                        float pzW = FrustumPlanes[23];
                        return nxX * x + nxY * y + nxZ * z + nxW >= -r &&
                               pxX * x + pxY * y + pxZ * z + pxW >= -r &&
                               nyX * x + nyY * y + nyZ * z + nyW >= -r &&
                               pyX * x + pyY * y + pyZ * z + pyW >= -r &&
                               nzX * x + nzY * y + nzZ * z + nzW >= -r &&
                               pzX * x + pzY * y + pzZ * z + pzW >= -r;
                    }
                                   
                    void kernel update_draw(global int* Counter, global const float* FrustumPlanes, const float4 CameraPos, global const float* LightData, global uint* DrawData) {
                        int lightId = get_global_id(0);
                        int lightDataIndex = lightId * %d;
                        float x = LightData[lightDataIndex + %d];
                        float y = LightData[lightDataIndex + %d];
                        float z = LightData[lightDataIndex + %d];
                        float range = LightData[lightDataIndex + %d];
                        
                        bool visible = testSphere(FrustumPlanes, x - CameraPos.x, y - CameraPos.y, z - CameraPos.z, range * 1.414);
                        if (visible) {
                            int i = Counter[0];
                            DrawData[i] = %d;
                            DrawData[i + 1] = 1;
                            DrawData[i + 2] = %d;
                            DrawData[i + 3] = 0;
                            DrawData[i + 4] = lightId;
                            atomic_inc(Counter);
                        }
                    }
                    """.formatted(lightSize, positionOffset, positionOffset + 1, positionOffset + 2, rangeOffset, this.highResSize, 0));

            try {
                kernel = ENVIRONMENT.createKernel(name, "update_draw");
                clCounter = kernel.createBuffer(CL_MEM_READ_WRITE, Integer.BYTES);
                clFrustumPlanes = kernel.createBuffer(CL_MEM_READ_ONLY, 24 * Float.BYTES);
                clInstancedBuffer = kernel.createBufferFromGL(CL_MEM_WRITE_ONLY, this.instancedVbo);
                clIndirectBuffer = kernel.createBufferFromGL(CL_MEM_READ_ONLY, this.indirectVbo);
                kernel.setPointers(0, clCounter);
                kernel.setPointers(1, clFrustumPlanes);
                kernel.setPointers(3, clInstancedBuffer);
                kernel.setPointers(4, clIndirectBuffer);
            } catch (Exception e) {
                Veil.LOGGER.error("Failed to create indirect kernel", e);
            }
        }
        this.kernel = kernel;
        this.clCounter = clCounter;
        this.clFrustumPlanes = clFrustumPlanes;
        this.clInstancedBuffer = clInstancedBuffer;
        this.clIndirectBuffer = clIndirectBuffer;
    }

    /**
     * @return The mesh data each instanced light will be rendered with use
     */
    protected BufferBuilder.RenderedBuffer createMesh() {
        return LightTypeRenderer.createQuad();
    }

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

    private boolean shouldDrawHighResolution(T light, CullFrustum frustum) {
        float radius = light.getRadius();
        return frustum.getPosition().distanceSquared(light.getPosition()) <= radius * radius;
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
            try (MemoryStack stack = MemoryStack.stackPush()) {
                this.clInstancedBuffer.acquireFromGL();
                this.clIndirectBuffer.acquireFromGL();

                this.clCounter.writeAsync(0L, stack.ints(0), null);

                ByteBuffer planes = stack.malloc(6 * 4 * Float.BYTES);
                int index = 0;
                for (Vector4fc plane : frustum.getPlanes()) {
                    plane.get(index, planes);
                    index += 4 * Float.BYTES;
                }
                this.clFrustumPlanes.writeAsync(0L, planes, null);

                Vector3dc pos = frustum.getPosition();
                this.kernel.setVector4f(2, (float) pos.x(), (float) pos.y(), (float) pos.z(), 0);
                this.kernel.execute(lights.size(), 1);

                this.clInstancedBuffer.releaseToGL();
                this.clIndirectBuffer.releaseToGL();

                IntBuffer data = stack.mallocInt(1);
                this.clCounter.read(0L, data);
                return data.get(0);
            } catch (CLException e) {
                Veil.LOGGER.error("Failed to run indirect compute", e);
                this.freeCL();
            }
        }

        int count = 0;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(this.lowResSize > 0 ? Integer.BYTES * 5 : Integer.BYTES);

            int index = 0;
            for (T light : lights) {
                if (light.isVisible(frustum)) {
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
    public void renderLights(LightRenderer lightRenderer, List<T> lights, Set<T> removedLights, CullFrustum frustum) {
        VertexBufferExtension ext = (VertexBufferExtension) this.vbo;
        this.vbo.bind();
        glBindBuffer(GL_ARRAY_BUFFER, this.instancedVbo);
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, this.indirectVbo);

        // If there is no space, then resize
        boolean rebuild = false;
        if (lights.size() > this.maxLights || this.maxLights > lights.size() * 2) {
            rebuild = true;
            this.maxLights = (int) Math.max(Math.ceil(this.maxLights / 2.0), lights.size() * 1.5);
            glBufferData(GL_ARRAY_BUFFER, (long) this.maxLights * this.lightSize, GL_DYNAMIC_DRAW);
            glBufferData(GL_DRAW_INDIRECT_BUFFER, (long) this.maxLights * Integer.BYTES * 5, GL_STREAM_DRAW);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer buffer = stack.calloc(Integer.BYTES * 5);
                buffer.putInt(0, ext.veil$getIndexCount());
                buffer.putInt(4, 1);

                for (int i = 0; i < this.maxLights; i++) {
                    glBufferSubData(GL_DRAW_INDIRECT_BUFFER, i * Integer.BYTES * 5L, buffer);
                }
            }
        }

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

        // Fill indirect buffer draw calls
        int count = !lights.isEmpty() ? this.updateVisibility(lights, frustum) : 0;
        if (count > 0) {
            this.setupRenderState(lightRenderer, lights);
            lightRenderer.applyShader();
            ext.veil$drawIndirect(0L, count, 0);
            this.clearRenderState(lightRenderer, lights);
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);
        VertexBuffer.unbind();
    }

    private void freeCL() {
        if (this.kernel != null) {
            this.kernel.free();
            this.kernel = null;
        }
        if (this.clCounter != null) {
            this.clCounter.free();
        }
        if (this.clFrustumPlanes != null) {
            this.clFrustumPlanes.free();
        }
        if (this.clInstancedBuffer != null) {
            this.clInstancedBuffer.free();
        }
        if (this.clIndirectBuffer != null) {
            this.clIndirectBuffer.free();
        }
    }

    @Override
    public void free() {
        this.vbo.close();
        glDeleteBuffers(this.instancedVbo);
        glDeleteBuffers(this.indirectVbo);
        this.freeCL();
    }
}
