package foundry.veil.render.deferred.light;

import java.nio.ByteBuffer;

public interface InstancedLight {

    /**
     * Stores the data of this light into the specified buffer.
     *
     * @param buffer The buffer to fill
     */
    void store(ByteBuffer buffer);
}
