package foundry.veil.api.client.render.deferred.light;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.ext.VertexBufferExtension;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL40C.GL_DRAW_INDIRECT_BUFFER;

/**
 * Draws lights as indirect instanced quads in the scene.
 *
 * @param <T> The type of lights to render
 * @author Ocelot
 */
public abstract class IndirectLightRenderer<T extends Light & InstancedLight> implements LightTypeRenderer<T> {

    protected final int lightSize;
    protected final int highResSize;
    protected final int lowResSize;
    protected int maxLights;

    private final VertexBuffer vbo;
    private final int instancedVbo;
    private final int indirectVbo;

    /**
     * Creates a new instanced light renderer with a resizeable light buffer.
     *
     * @param lightSize  The size of each light in bytes
     * @param lowResSize The size of the low-resolution mesh or <code>0</code> to only use the high-detail mesh
     */
    public IndirectLightRenderer(int lightSize, int lowResSize) {
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

    protected boolean shouldDrawHighResolution(T light, CullFrustum frustum) {
        return true;
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
        // TODO move to compute if supported
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

    @Override
    public void free() {
        this.vbo.close();
        glDeleteBuffers(this.instancedVbo);
        glDeleteBuffers(this.indirectVbo);
    }
}
