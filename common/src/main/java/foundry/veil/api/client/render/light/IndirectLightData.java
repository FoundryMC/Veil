package foundry.veil.api.client.render.light;

import foundry.veil.api.client.render.light.renderer.InstancedLightRenderer;
import org.joml.Vector3dc;

/**
 * A light that can be rendered with an implementation of {@link InstancedLightRenderer}.
 *
 * @since 2.0.0
 */
public interface IndirectLightData extends InstancedLightData {

    /**
     * @return The position of this light
     */
    Vector3dc getPosition();

    /**
     * @return The maximum distance the light can travel
     */
    float getRadius();
}
