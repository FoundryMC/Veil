package foundry.veil.impl.client.render.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import foundry.veil.api.client.render.vertex.VertexArray;
import foundry.veil.mixin.pipeline.accessor.PipelineAutoStorageIndexBufferAccessor;
import org.jetbrains.annotations.ApiStatus;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferData;
import static org.lwjgl.opengl.ARBDirectStateAccess.glVertexArrayElementBuffer;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;

@ApiStatus.Internal
public class DSAVertexArray extends VertexArray {

    public DSAVertexArray(int id) {
        super(id, vao -> new DSAVertexAttribBindingBuilder(vao, id));
    }

    @Override
    public void uploadIndexBuffer(ByteBuffer data) {
        int elementArrayBuffer = this.getOrCreateBuffer(ELEMENT_ARRAY_BUFFER);
        glNamedBufferData(elementArrayBuffer, data, GL_STATIC_DRAW);
        glVertexArrayElementBuffer(this.id, elementArrayBuffer);
    }

    @Override
    public void uploadIndexBuffer(MeshData.DrawState drawState) {
        super.uploadIndexBuffer(drawState);
        PipelineAutoStorageIndexBufferAccessor ext = (PipelineAutoStorageIndexBufferAccessor) (Object) RenderSystem.getSequentialBuffer(drawState.mode());
        glVertexArrayElementBuffer(this.id, ext.getName());
    }
}
