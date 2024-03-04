package foundry.veil.api.client.render.deferred.light.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.deferred.light.IndirectLight;
import foundry.veil.api.client.render.deferred.light.Light;
import foundry.veil.api.client.render.shader.VeilShaders;
import foundry.veil.api.client.render.shader.definition.DynamicShaderBlock;
import foundry.veil.api.client.render.shader.definition.ShaderBlock;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.ext.VertexBufferExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Vector3d;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Set;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL40C.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL42C.*;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.glDispatchCompute;

/**
 * Draws lights as indirect instanced quads in the scene.
 *
 * @param <T> The type of lights to render
 * @author Ocelot
 */
public abstract class IndirectLightRenderer<T extends Light & IndirectLight<T>> implements LightTypeRenderer<T> {

    private static final int MIN_LIGHTS = 20;

    protected final int lightSize;
    protected final int highResSize;
    protected final int lowResSize;
    protected final int positionOffset;
    protected final int rangeOffset;
    protected int maxLights;

    private final VertexBuffer vbo;
    private final int instancedVbo;
    private final int indirectVbo;
    private final int sizeVbo;
    private final DynamicShaderBlock<?> instancedBlock;
    private final DynamicShaderBlock<?> indirectBlock;

    private int visibleLights;

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

        if (VeilRenderSystem.computeSupported() && VeilRenderSystem.atomicCounterSupported()) {
            this.sizeVbo = glGenBuffers();
            this.instancedBlock = ShaderBlock.wrapper(GL_SHADER_STORAGE_BUFFER, this.instancedVbo);
            this.indirectBlock = ShaderBlock.wrapper(GL_SHADER_STORAGE_BUFFER, this.indirectVbo);

            glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, this.sizeVbo);
            glBufferData(GL_ATOMIC_COUNTER_BUFFER, Integer.BYTES, GL_DYNAMIC_DRAW);
            glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, 0);
        } else {
            this.sizeVbo = 0;
            this.instancedBlock = null;
            this.indirectBlock = null;
        }

        this.vbo.bind();
        this.vbo.upload(this.createMesh());

        VertexBufferExtension ext = (VertexBufferExtension) this.vbo;
        this.highResSize = ext.veil$getIndexCount() - lowResSize;
        this.lowResSize = lowResSize;
        this.positionOffset = positionOffset;
        this.rangeOffset = rangeOffset;

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
        this.instancedBlock.setSize((long) this.maxLights * this.lightSize);
        this.indirectBlock.setSize((long) this.maxLights * Integer.BYTES * 5);

//        if (this.kernel != null) {
//            try {
//                if (this.clInstancedBuffer != null) {
//                    this.clInstancedBuffer.free();
//                }
//                if (this.clIndirectBuffer != null) {
//                    this.clIndirectBuffer.free();
//                }
//                this.clInstancedBuffer = this.kernel.createBufferFromGL(CL_MEM_READ_ONLY, this.instancedVbo);
//                this.clIndirectBuffer = this.kernel.createBufferFromGL(CL_MEM_WRITE_ONLY, this.indirectVbo);
//                this.kernel.setPointers(3, this.clInstancedBuffer);
//                this.kernel.setPointers(4, this.clIndirectBuffer);
//            } catch (CLException e) {
//                Veil.LOGGER.error("Failed to initialize indirect compute", e);
//                this.freeCL();
//            }
//        } else {
//            try (MemoryStack stack = MemoryStack.stackPush()) {
//                ByteBuffer buffer = stack.calloc(Integer.BYTES * 5);
//                buffer.putInt(0, ((VertexBufferExtension) this.vbo).veil$getIndexCount());
//                buffer.putInt(4, 1);
//
//                for (int i = 0; i < this.maxLights; i++) {
//                    glBufferSubData(GL_DRAW_INDIRECT_BUFFER, i * Integer.BYTES * 5L, buffer);
//                }
//            }
//        }
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
        for (int i = 0; i < lights.size(); i++) {
            T light = lights.get(i);
            light.clean();
            dataBuffer.position(i * this.lightSize);
            light.store(dataBuffer);
        }

        dataBuffer.rewind();
        glBufferSubData(GL_ARRAY_BUFFER, 0, dataBuffer);
        MemoryUtil.memFree(dataBuffer);
    }

    private int updateVisibility(List<T> lights, CullFrustum frustum) {
//        if (this.kernel != null) {
//            ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
//            try (MemoryStack stack = MemoryStack.stackPush()) {
//                profiler.push("acquire");
//                this.kernel.acquireFromGL(this.clInstancedBuffer, this.clIndirectBuffer);
//                profiler.popPush("upload");
//
//                this.clCounter.writeAsync(0L, stack.ints(0), null);
//
//                ByteBuffer planes = stack.malloc(6 * 4 * Float.BYTES);
//                int index = 0;
//                for (Vector4fc plane : frustum.getPlanes()) {
//                    plane.get(index, planes);
//                    index += 4 * Float.BYTES;
//                }
//                this.clFrustumPlanes.writeAsync(0L, planes, null);
//
//                profiler.popPush("setup");
//                Vector3dc pos = frustum.getPosition();
//                this.kernel.setVector4f(0, (float) pos.x(), (float) pos.y(), (float) pos.z(), 0);
//                this.kernel.execute(lights.size(), 1);
//
//                profiler.popPush("release");
//                this.kernel.releaseToGL(this.clInstancedBuffer, this.clIndirectBuffer);
//
//                profiler.popPush("read");
//                IntBuffer data = stack.mallocInt(1);
//                this.clCounter.read(0L, data);
//                profiler.pop();
//                return data.get(0);
//            } catch (CLException e) {
//                Veil.LOGGER.error("Failed to run indirect compute", e);
//                this.freeCL();
//            }
//        }

        if (this.sizeVbo != 0) {
            VeilRenderSystem.setShader(VeilShaders.LIGHT_INDIRECT_SPHERE);
            ShaderProgram shader = VeilRenderSystem.getShader();
            if (shader != null) {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    VeilRenderSystem.bind("VeilLightInstanced", this.instancedBlock);
                    VeilRenderSystem.bind("VeilLightIndirect", this.indirectBlock);

                    glBindBufferRange(GL_ATOMIC_COUNTER_BUFFER, 0, this.sizeVbo, 0, Integer.BYTES);
                    glBufferSubData(GL_ATOMIC_COUNTER_BUFFER, 0, stack.callocInt(1));

                    shader.setInt("HighResSize", this.highResSize);
                    shader.setInt("LowResSize", this.lowResSize);
                    shader.setInt("LightSize", this.lightSize / Float.BYTES);
                    shader.setInt("PositionOffset", this.positionOffset);
                    shader.setInt("RangeOffset", this.rangeOffset);

                    Vector4fc[] planes = frustum.getPlanes();
                    float[] values = new float[4 * planes.length];
                    for (int i = 0; i < planes.length; i++) {
                        Vector4fc plane = planes[i];
                        values[i * 4] = plane.x();
                        values[i * 4 + 1] = plane.y();
                        values[i * 4 + 2] = plane.z();
                        values[i * 4 + 3] = plane.w();
                    }
                    shader.setFloats("FrustumPlanes", values);

                    shader.bind();

                    glDispatchCompute(Math.min(lights.size(), VeilRenderSystem.maxComputeWorkGroupCountX()), 1, 1);
                    glMemoryBarrier(GL_BUFFER_UPDATE_BARRIER_BIT | GL_ATOMIC_COUNTER_BARRIER_BIT);

                    ShaderProgram.unbind();

                    IntBuffer counter = stack.mallocInt(1);
                    glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, this.sizeVbo);
                    glGetBufferSubData(GL_ATOMIC_COUNTER_BUFFER, 0L, counter);
                    glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, 0);
                    return counter.get(0);
                } finally {
                    VeilRenderSystem.unbind(this.instancedBlock);
                    VeilRenderSystem.unbind(this.indirectBlock);
                    glBindBufferRange(GL_ATOMIC_COUNTER_BUFFER, 0, 0, 0, Integer.BYTES);
                }
            } else {
                VeilRenderSystem.throwShaderError();
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
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        profiler.popPush("visibility");

        // Fill indirect buffer draw calls
        this.visibleLights = !lights.isEmpty() ? this.updateVisibility(lights, frustum) : 0;

        profiler.pop();
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

    @Override
    public int getVisibleLights() {
        return this.visibleLights;
    }

    @Override
    public void free() {
        this.vbo.close();
        glDeleteBuffers(this.instancedVbo);
        glDeleteBuffers(this.indirectVbo);
        if (this.sizeVbo != 0) {
            glDeleteBuffers(this.sizeVbo);
            this.instancedBlock.free();
            this.indirectBlock.free();
        }
    }
}
