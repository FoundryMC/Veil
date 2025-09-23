package foundry.veil.api.flare.data.model;

import foundry.veil.api.flare.model.BakedShell;

import java.util.ArrayList;
import java.util.List;

public class SimpleBakedShell implements BakedShell {
    protected final List<FlareBakedQuad> faces;

    public SimpleBakedShell(List<FlareBakedQuad> faces) {
        this.faces = faces;
    }

    @Override
    public List<FlareBakedQuad> getQuads() {
        return faces;
    }

    public static class Builder {
        protected final List<FlareBakedQuad> faces = new ArrayList<>();
        public Builder addFace(FlareBakedQuad face) {
            faces.add(face);
            return this;
        }

        public SimpleBakedShell build() {
            return new SimpleBakedShell(faces);
        }
    }
}
