package foundry.veil.impl.client.render.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import foundry.veil.api.client.render.vertex.VertexArray;
import foundry.veil.ext.AutoStorageIndexBufferExtension;
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
        this.indexBuffer = null;
        int elementArrayBuffer = this.getOrCreateBuffer(ELEMENT_ARRAY_BUFFER);
        glNamedBufferData(elementArrayBuffer, data, GL_STATIC_DRAW);
        glVertexArrayElementBuffer(this.id, elementArrayBuffer);
    }

    @Override
    public void uploadIndexBuffer(MeshData.DrawState drawState) {
        this.indexBuffer = RenderSystem.getSequentialBuffer(drawState.mode());
        AutoStorageIndexBufferExtension ext = (AutoStorageIndexBufferExtension) (Object) this.indexBuffer;
        ext.veil$ensureStorage(drawState.indexCount());
        glVertexArrayElementBuffer(this.id, ext.veil$getBuffer());
    }
}
