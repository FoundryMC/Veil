package foundry.veil.api.flare.model;

import foundry.veil.api.flare.data.model.FlareBakedQuad;

import java.util.List;

public interface BakedShell {
    List<FlareBakedQuad> getQuads();
}
