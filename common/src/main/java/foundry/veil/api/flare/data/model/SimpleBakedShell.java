package foundry.veil.api.flare.data.model;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.vertex.VertexArray;
import foundry.veil.api.flare.model.BakedShell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @since 2.5.0
 */
public class SimpleBakedShell implements BakedShell {

    protected final List<FlareBakedQuad> faces;
    private VertexArray vertexArray;

    public SimpleBakedShell(List<FlareBakedQuad> faces) {
        this.faces = Collections.unmodifiableList(faces);
    }

    @Override
    public List<FlareBakedQuad> getQuads() {
        return this.faces;
    }

    @Override
    public VertexArray getVertexArray() {
        if (this.vertexArray == null) {
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

            for (FlareBakedQuad quad : this.faces) {
                quad.putBakedQuadInto(builder);
            }

            this.vertexArray = VertexArray.create();
            this.vertexArray.upload(builder.buildOrThrow(), VertexArray.DrawUsage.STATIC);
        }
        return this.vertexArray;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void free() {
        if (this.vertexArray != null) {
            this.vertexArray.free();
            this.vertexArray = null;
        }
    }

    public static class Builder {

        protected final List<FlareBakedQuad> faces = new ArrayList<>();

        public Builder addFace(FlareBakedQuad face) {
            this.faces.add(face);
            return this;
        }

        public SimpleBakedShell build() {
            return new SimpleBakedShell(this.faces);
        }
    }
}
