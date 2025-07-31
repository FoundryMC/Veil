package foundry.veil.impl.client.render.vertex;

import foundry.veil.api.client.render.vertex.VertexArray;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ARBVertexArray extends VertexArray {

    public ARBVertexArray(int id) {
        super(id, ARBVertexAttribBindingBuilder::new);
    }
}
