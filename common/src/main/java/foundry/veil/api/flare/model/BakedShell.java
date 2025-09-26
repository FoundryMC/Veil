package foundry.veil.api.flare.model;

import foundry.veil.api.flare.data.model.FlareBakedQuad;

import java.util.List;

/**
 * Baked Shell, contains a list of baked quads.
 * Shells are models stripped of their texture, though they may still contain texture coordinates.
 *
 * @author GuyApooye
*/
public interface BakedShell {
    List<FlareBakedQuad> getQuads();
}
