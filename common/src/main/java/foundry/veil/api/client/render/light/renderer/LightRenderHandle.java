package foundry.veil.api.client.render.light.renderer;

import foundry.veil.api.client.render.light.data.LightData;
import org.lwjgl.system.NativeResource;

/**
 * @since 2.0.0
 */
public interface LightRenderHandle<T extends LightData> extends NativeResource {

    /**
     * @return The data associated with this handle
     */
    T getLightData();

    /**
     * Marks the data in this light as dirty and needing re-uploading.
     */
    void markDirty();

    /**
     * @return Whether this light is currently owned by a renderer
     */
    boolean isValid();
}
