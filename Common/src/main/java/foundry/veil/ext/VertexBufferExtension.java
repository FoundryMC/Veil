package foundry.veil.ext;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface VertexBufferExtension {

    void veil$drawInstanced(int instances);
}
