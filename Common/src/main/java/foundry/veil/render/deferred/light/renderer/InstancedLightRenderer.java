package foundry.veil.render.deferred.light.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import foundry.veil.ext.VertexBufferExtension;
import foundry.veil.render.deferred.LightRenderer;
import foundry.veil.render.deferred.light.InstancedLight;
import foundry.veil.render.deferred.light.Light;
import foundry.veil.render.wrapper.CullFrustum;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15C.*;

/**
 * Draws lights as instanced quads in the scene.
 *
 * @param <T> The type of lights to renders
 * @author Ocelot
 */
public abstract class InstancedLightRenderer<T extends Light & InstancedLight> implements LightTypeRenderer<T> {

    protected final int lightSize;
    protected int maxLights;

    private final List<T> visibleLights;
    private final VertexBuffer vbo;
    private final int instancedVbo;

    private int oldSize;

    /**
     * Creates a new instanced light renderer with a resizeable light buffer.
     *
     * @param maxLights The maximum amount of lights that can be rendered in one draw call.
     * @param lightSize The size of each light in bytes
     */
    public InstancedLightRenderer(int maxLights, int lightSize) {
        this.maxLights = maxLights;
        this.lightSize = lightSize;
        this.visibleLights = new ArrayList<>();
        this.vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.instancedVbo = glGenBuffers();

        this.vbo.bind();
        this.vbo.upload(this.createMesh());

        glBindBuffer(GL_ARRAY_BUFFER, this.instancedVbo);
        glBufferData(GL_ARRAY_BUFFER, (long) maxLights * this.lightSize, GL_DYNAMIC_DRAW);
        this.setupBufferState();
        glBindBuffer(GL_ARRAY_BUFFER, 0);

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

    private void updateAllLights(List<T> lights) {
        this.oldSize = lights.size();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            int pointer = 0;
            ByteBuffer dataBuffer = stack.malloc(lights.size() * this.lightSize);
            for (T light : lights) {
                light.clean();
                dataBuffer.position((pointer++) * this.lightSize);
                light.store(dataBuffer);
            }

            dataBuffer.rewind();
            glBufferSubData(GL_ARRAY_BUFFER, 0L, dataBuffer);
        }
    }

    @Override
    public void renderLights(LightRenderer lightRenderer, List<T> lights, CullFrustum frustum) {
        this.visibleLights.clear();
        for (T light : lights) {
            if (light.isVisible(frustum)) {
                this.visibleLights.add(light);
            }
        }

        if (this.visibleLights.isEmpty()) {
            return;
        }

        this.vbo.bind();
        glBindBuffer(GL_ARRAY_BUFFER, this.instancedVbo);

        // If there is no space, then resize
        if (this.visibleLights.size() > this.maxLights) {
            this.oldSize = 0;
            this.maxLights += (int) Math.ceil(this.maxLights / 2.0);
            glBufferData(GL_ARRAY_BUFFER, (long) this.maxLights * this.lightSize, GL_STREAM_DRAW);
        }

        if (this.oldSize != this.visibleLights.size()) {
            this.updateAllLights(this.visibleLights);
        } else {
            for (int i = 0; i < this.visibleLights.size(); i++) {
                T light = this.visibleLights.get(i);
                if (light.isDirty()) {
                    light.clean();
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        ByteBuffer buffer = stack.malloc(this.lightSize);
                        light.store(buffer);
                        buffer.rewind();
                        glBufferSubData(GL_ARRAY_BUFFER, (long) i * this.lightSize, buffer);
                    }
                }
            }
        }

        this.setupRenderState(lightRenderer, this.visibleLights);
        lightRenderer.applyShader();

        ((VertexBufferExtension) this.vbo).drawInstanced(this.visibleLights.size());

        this.clearRenderState(lightRenderer, this.visibleLights);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        VertexBuffer.unbind();
    }

    @Override
    public void free() {
        this.vbo.close();
        glDeleteBuffers(this.instancedVbo);
    }
}
