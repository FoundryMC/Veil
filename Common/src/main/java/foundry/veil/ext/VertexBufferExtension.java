package foundry.veil.ext;

import com.mojang.blaze3d.vertex.VertexBuffer;

/**
 * Extra functionality for {@link VertexBuffer}.
 */
public interface VertexBufferExtension {

    /**
     * Draws the specified number of instances of this vertex buffer.
     *
     * @param instances The number of instances to draw
     */
    default void drawInstanced(int instances) {
        throw new UnsupportedOperationException();
    }
}
