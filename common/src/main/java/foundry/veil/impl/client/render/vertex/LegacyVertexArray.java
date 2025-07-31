package foundry.veil.impl.client.render.vertex;

import foundry.veil.api.client.render.vertex.VertexArray;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class LegacyVertexArray extends VertexArray {

    public LegacyVertexArray(int id) {
        super(id, LegacyVertexAttribBindingBuilder::new);
    }
}
