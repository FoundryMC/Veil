package foundry.veil.api.client.render.light;

import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.api.client.render.MatrixStack;

/**
 * Provides extra information for given light data by rendering guides into the world.
 *
 * @author Neddslayer
 */
public interface LightGuideProvider {

    /**
     * Render the light guides using the VertexConsumer.
     */
    void renderLightGuide(MatrixStack stack, VertexConsumer consumer);

}
