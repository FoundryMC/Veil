package foundry.veil.ext;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface VertexBufferExtension {

    void veil$drawInstanced(int instances);

    void veil$drawIndirect(long indirect, int drawCount, int stride);

    int veil$getIndexCount();
}
