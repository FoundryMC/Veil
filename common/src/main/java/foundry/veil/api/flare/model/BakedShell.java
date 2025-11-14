package foundry.veil.api.flare.model;

import foundry.veil.api.client.render.vertex.VertexArray;
import foundry.veil.api.flare.data.model.FlareBakedQuad;
import org.lwjgl.system.NativeResource;

import java.util.Collection;

/**
 * Baked Shell, contains a list of baked quads.
 * Shells are models stripped of their texture, though they may still contain texture coordinates.
 *
 * @author GuyApooye
 * @since 2.5.0
 */
public interface BakedShell extends NativeResource {

    Collection<FlareBakedQuad> getQuads();

    VertexArray getVertexArray();
}
