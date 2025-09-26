package foundry.veil.api.flare;

import com.mojang.blaze3d.vertex.MeshData;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.vertex.VertexArray;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link VertexArray} extension used by Flare to apply uniforms before rendering.
 *
 * @author GuyApooye
 */
public class FlareVertexArrayExtension {

    private final VertexArray vertexArray;
    private final List<Runnable> setups = new ArrayList<>();
    private final List<Runnable> clears = new ArrayList<>();

    public FlareVertexArrayExtension(VertexArray vertexArray) {
        this.vertexArray = vertexArray;
    }

    /**
     * Add a runnable that is run before rendering
     */
    public void addSetup(Runnable setup) {
        setups.add(setup);
    }

    /**
     * Add a runnable that is run after rendering
     */
    public void addClear(Runnable clear) {
        clears.add(clear);
    }

    /**
     * Resets setup list
     */
    public void resetSetups() {
        setups.clear();
    }

    /**
     * Resets clear list
     */
    public void resetClears() {
        clears.clear();
    }


    private void applySetups() {
        for (Runnable setup : setups) {
            setup.run();
        }
    }

    private void applyClears() {
        for (Runnable clear : clears) {
            clear.run();
        }
    }

    /**
     * Sets up the given renderType and executes all setup runnables.
     */
    public void setup(RenderType renderType) {
        vertexArray.setup(renderType);
        this.applySetups();
    }

    /**
     * Clears the given renderType and executes all clear runnables.
     */
    public void clear(RenderType renderType) {
        vertexArray.clear(renderType);
        this.applyClears();
    }

    public void drawWithRenderType(RenderType renderType) {
        while (renderType instanceof VeilRenderType.RenderTypeWrapper wrapper) {
            renderType = wrapper.get();
        }

        if (renderType == null) {
            return;
        }

        this.setup(renderType);
        vertexArray.draw();
        this.clear(renderType);

        if (renderType instanceof VeilRenderType.LayeredRenderType layeredRenderType) {
            for (RenderType layer : layeredRenderType.getLayers()) {
                this.setup(layer);
                vertexArray.draw();
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
        vertexArray.drawInstanced(instances);
        this.clear(renderType);

        if (renderType instanceof VeilRenderType.LayeredRenderType layeredRenderType) {
            for (RenderType layer : layeredRenderType.getLayers()) {
                this.setup(layer);
                vertexArray.drawInstanced(instances);
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
        vertexArray.drawIndirect(indirect, drawCount, stride);
        this.clear(renderType);

        if (renderType instanceof VeilRenderType.LayeredRenderType layeredRenderType) {
            for (RenderType layer : layeredRenderType.getLayers()) {
                this.setup(layer);
                vertexArray.drawIndirect(indirect, drawCount, stride);
                this.clear(layer);
            }
        }

        this.resetSetups();
        this.resetClears();
    }

    public VertexArray getVertexArray() {
        return vertexArray;
    }
}
