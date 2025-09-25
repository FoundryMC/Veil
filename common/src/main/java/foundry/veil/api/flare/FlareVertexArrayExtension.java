package foundry.veil.api.flare;

import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.vertex.VertexArray;
import foundry.veil.api.client.render.vertex.VertexArrayBuilder;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Unique;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FlareVertexArrayExtension {

    private final VertexArray original;
    private final List<Runnable> setups = new ArrayList<>();
    private final List<Runnable> clears = new ArrayList<>();

    public FlareVertexArrayExtension(VertexArray original) {
        this.original = original;
    }

    public void addSetup(Runnable setup) {
        setups.add(setup);
    }

    public void addClear(Runnable clear) {
        clears.add(clear);
    }

    public void resetSetups() {
        setups.clear();
    }

    public void resetClears() {
        clears.clear();
    }

    public void applySetups() {
        for (Runnable setup : setups) {
            setup.run();
        }
    }

    public void applyClears() {
        for (Runnable clear : clears) {
            clear.run();
        }
    }

    public void setup(RenderType renderType) {
        original.setup(renderType);
        this.applySetups();
    }

    public void clear(RenderType renderType) {
        original.clear(renderType);
        this.applyClears();
    }

    public int getOrCreateBuffer(int index) {
        return original.getOrCreateBuffer(index);
    }

    public int getId() {
        return original.getId();
    }

    public int getIndexCount() {
        return original.getIndexCount();
    }

    public VertexArray.IndexType getIndexType() {
        return original.getIndexType();
    }

    public VertexFormat.Mode getDrawMode() {
        return original.getDrawMode();
    }

    public void upload(MeshData meshData, VertexArray.DrawUsage usage) {
        original.upload(0, meshData, usage);
    }

    public void upload(int attributeStart, MeshData meshData, VertexArray.DrawUsage usage) {
        original.upload(attributeStart, meshData, usage);
    }

    public void uploadIndexBuffer(MeshData.DrawState drawState) {
        original.uploadIndexBuffer(drawState);
    }

    public void uploadIndexBuffer(ByteBuffer data) {
        original.uploadIndexBuffer(data);
    }

    public void uploadIndexBuffer(ByteBuffer data, VertexArray.IndexType indexType) {
        original.uploadIndexBuffer(data, indexType);
    }

    public VertexArrayBuilder editFormat() {
        return original.editFormat();
    }

    public void bind() {
        original.bind();
    }

    public void draw() {
        original.draw();
    }

    public void drawInstanced(int instances) {
        original.drawInstanced(instances);
    }

    public void drawIndirect(long indirect, int drawCount, int stride) {
        original.drawIndirect(indirect, drawCount, stride);
    }

    public void drawWithRenderType(RenderType renderType) {
        while (renderType instanceof VeilRenderType.RenderTypeWrapper wrapper) {
            renderType = wrapper.get();
        }

        if (renderType == null) {
            return;
        }

        this.setup(renderType);
        this.draw();
        this.clear(renderType);

        if (renderType instanceof VeilRenderType.LayeredRenderType layeredRenderType) {
            for (RenderType layer : layeredRenderType.getLayers()) {
                this.setup(layer);
                this.draw();
                this.clear(layer);
            }
        }

        this.resetSetups();
        this.resetClears();
    }

    public void drawInstancedWithRenderType(RenderType renderType, int instances) {
        while (renderType instanceof VeilRenderType.RenderTypeWrapper wrapper) {
            renderType = wrapper.get();
        }

        if (renderType == null) {
            return;
        }

        this.setup(renderType);
        this.drawInstanced(instances);
        this.clear(renderType);

        if (renderType instanceof VeilRenderType.LayeredRenderType layeredRenderType) {
            for (RenderType layer : layeredRenderType.getLayers()) {
                this.setup(layer);
                this.drawInstanced(instances);
                this.clear(layer);
            }
        }

        this.resetSetups();
        this.resetClears();
    }

    public void drawIndirectWithRenderType(RenderType renderType, long indirect, int drawCount, int stride) {
        while (renderType instanceof VeilRenderType.RenderTypeWrapper wrapper) {
            renderType = wrapper.get();
        }

        if (renderType == null) {
            return;
        }

        this.setup(renderType);
        this.drawIndirect(indirect, drawCount, stride);
        this.clear(renderType);

        if (renderType instanceof VeilRenderType.LayeredRenderType layeredRenderType) {
            for (RenderType layer : layeredRenderType.getLayers()) {
                this.setup(layer);
                this.drawIndirect(indirect, drawCount, stride);
                this.clear(layer);
            }
        }

        this.resetSetups();
        this.resetClears();
    }

    /**
     * Sets the number of indices and what data type they are.
     *
     * @param indexCount The number of indices in the entire mesh
     * @param indexType  The data type of the indices
     */
    public void setIndexCount(int indexCount, VertexArray.IndexType indexType) {
        original.setIndexCount(indexCount, indexType);
    }

    /**
     * Sets the type of polygons draw calls will draw.
     *
     * @param drawMode The new draw mode
     */
    public void setDrawMode(VertexFormat.Mode drawMode) {
        original.setDrawMode(drawMode);
    }

    public void free() {
        original.free();
    }

    public VertexArray getVertexArray() {
        return original;
    }
}
