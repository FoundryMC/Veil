package foundry.veil.api.client.render.light;

import foundry.veil.Veil;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;

/**
 * Allows a light to project a texture into the world.
 *
 * @author Neddslayer
 * @since 4.6.0
 */
public interface LightProjectorData {
    ResourceLocation PROJECTION_ATLAS = Veil.veilPath("textures/atlas/light_projection.png");

    ResourceLocation getTextureLocation();

    Vector2f getTextureUVMin();
    Vector2f getTextureUVMax();
}
