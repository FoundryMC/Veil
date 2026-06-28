package foundry.veil.api.client.editor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.api.client.render.MatrixStack;

/**
 * Provides extra information for given light data by rendering gizmos into the world.
 *
 * @author Neddslayer
 */
public interface LightGuideProvider {

    /**
     * Render the light gizmos using the VertexConsumer.
     */
    void renderLightHelper(MatrixStack stack, VertexConsumer consumer);
}
