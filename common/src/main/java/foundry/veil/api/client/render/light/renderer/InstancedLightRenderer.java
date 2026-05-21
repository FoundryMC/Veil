package foundry.veil.api.client.render.light.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.light.InstancedLightData;
import foundry.veil.api.client.render.light.data.LightData;
import foundry.veil.api.client.render.vertex.VertexArray;
import foundry.veil.api.client.render.vertex.VertexArrayBuilder;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.system.MemoryUtil.memAddress;

/**
 * Draws lights as instanced quads in the scene.
 *
 * @param <T> The type of lights to render
 * @author Ocelot
 */
public abstract class InstancedLightRenderer<T extends LightData & InstancedLightData> implements LightTypeRenderer<T> {

    private static final int MAX_UPLOADS = 400;

    protected final int lightSize;
    protected int maxLights;

    private final List<LightHandle> lights;
    private final List<LightHandle> visibleLights;
    private final List<LightHandle> lastVisibleLights;
    private final VertexArray vertexArray;
    private final int instancedVbo;

    private boolean freed;

    /**
     * Creates a new instanced light renderer with a resizeable light buffer.
     *
     * @param lightSize The size of each light in bytes
     */
    public InstancedLightRenderer(int lightSize) {
        this.lightSize = lightSize;
        this.maxLights = 0;
        this.lights = new ArrayList<>();
        this.visibleLights = new ArrayList<>();
        this.lastVisibleLights = new ArrayList<>();
        this.vertexArray = VertexArray.create();

        MeshData mesh = this.createMesh();
        this.vertexArray.upload(mesh, VertexArray.DrawUsage.STATIC);
        this.instancedVbo = this.vertexArray.getOrCreateBuffer(2);

        VertexArrayBuilder builder = this.vertexArray.editFormat();
        builder.defineVertexBuffer(2, this.instancedVbo, 0, this.lightSize, 1);
        this.setupBufferState(builder);
    }

    /**
     * @return The mesh data each instanced light will be rendered with use
     */
    protected abstract MeshData createMesh();

    /**
     * Sets up the instanced buffer state.
     */
    protected abstract void setupBufferState(VertexArrayBuilder builder);

    /**
     * Calculates the render type to use for the specified lights.
     *
     * @param lights All lights in the order they are in the instanced buffer
     * @return The render type to use
     */
    protected abstract @Nullable RenderType getRenderType(List<? extends LightRenderHandle<T>> lights);

    private void updateAllLights() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int pointer = 0;
            long offset = 0;
            ByteBuffer dataBuffer = stack.malloc(Math.min(MAX_UPLOADS, this.visibleLights.size()) * this.lightSize);
            for (LightHandle handle : this.visibleLights) {
                dataBuffer.position((pointer++) * this.lightSize);
                handle.data.store(dataBuffer);
                handle.uploadedRevision = handle.data.getRevision();
                if (pointer >= MAX_UPLOADS) {
                    dataBuffer.rewind();
                    glBufferSubData(GL_ARRAY_BUFFER, offset, dataBuffer);
                    offset += dataBuffer.capacity();
                    pointer = 0;
                }
            }

            if (pointer > 0) {
                dataBuffer.rewind();
                nglBufferSubData(GL_ARRAY_BUFFER, offset, (long) pointer * this.lightSize, memAddress(dataBuffer));
            }
        }
    }

    private void updateDirtyLights() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer dataBuffer = stack.malloc(this.lightSize);
            for (int i = 0; i < this.visibleLights.size(); i++) {
                LightHandle handle = this.visibleLights.get(i);
                long revision = handle.data.getRevision();
                if (handle.uploadedRevision == revision) {
                    continue;
                }

                dataBuffer.clear();
                handle.data.store(dataBuffer);
                dataBuffer.rewind();
                nglBufferSubData(GL_ARRAY_BUFFER, (long) i * this.lightSize, this.lightSize, memAddress(dataBuffer));
                handle.uploadedRevision = revision;
            }
        }
    }

    private boolean visibleLightsChanged() {
        if (this.visibleLights.size() != this.lastVisibleLights.size()) {
            return true;
        }

        for (int i = 0; i < this.visibleLights.size(); i++) {
            if (this.visibleLights.get(i) != this.lastVisibleLights.get(i)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public LightRenderHandle<T> addLight(T light) {
        LightHandle handle = new LightHandle(light);
        this.lights.add(handle);
        return handle;
    }

    @Override
    public LightRenderHandle<T> steal(LightRenderHandle<T> handle) {
        if (!(handle instanceof LightHandle)) {
            handle.free();
            return this.addLight(handle.getLightData());
        }
        return handle;
    }

    @Override
    public void prepareLights(LightRenderer lightRenderer, CullFrustum frustum) {
        this.visibleLights.clear();
        for (LightHandle light : this.lights) {
            if (light.data.isVisible(frustum)) {
                this.visibleLights.add(light);
            }
        }
    }

    @Override
    public void renderLights(LightRenderer lightRenderer) {
        if (this.visibleLights.isEmpty()) {
            return;
        }

        RenderType renderType = this.getRenderType(this.visibleLights);
        if (renderType == null) {
            return;
        }

        RenderSystem.glBindBuffer(GL_ARRAY_BUFFER, this.instancedVbo);

        boolean resized = false;

        // If there is no space, then resize
        if (this.visibleLights.size() > this.maxLights) {
            if (this.maxLights < 100) {
                this.maxLights = 100;
            } else {
                this.maxLights = (int) Math.max(Math.ceil(this.maxLights / 2.0), this.visibleLights.size() * 1.5);
            }
            glBufferData(GL_ARRAY_BUFFER, (long) this.maxLights * this.lightSize, GL_STREAM_DRAW);
            resized = true;
        }

        if (resized || this.visibleLightsChanged()) {
            this.updateAllLights();
            this.lastVisibleLights.clear();
            this.lastVisibleLights.addAll(this.visibleLights);
        } else {
            this.updateDirtyLights();
        }

        this.vertexArray.bind();
        this.vertexArray.drawInstancedWithRenderType(renderType, this.visibleLights.size());
    }

    @Override
    public List<? extends LightRenderHandle<T>> getLights() {
        return this.lights;
    }

    @Override
    public int getVisibleLights() {
        return this.visibleLights.size();
    }

    @Override
    public void free() {
        this.vertexArray.free();
        this.freed = true;
    }

    private class LightHandle implements LightRenderHandle<T> {

        private final T data;
        private long uploadedRevision;

        private LightHandle(T data) {
            this.data = data;
            this.uploadedRevision = data.getRevision() - 1;
        }

        @Override
        public T getLightData() {
            return this.data;
        }

        @Override
        public void markDirty() {
            this.data.markDirty();
        }

        @Override
        public boolean isValid() {
            return !InstancedLightRenderer.this.freed;
        }

        @Override
        public void free() {
            InstancedLightRenderer.this.lights.remove(this);
            InstancedLightRenderer.this.lastVisibleLights.remove(this);
        }
    }
}
