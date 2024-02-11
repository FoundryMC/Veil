package foundry.veil.mixin.client.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.ext.VertexBufferExtension;
import org.lwjgl.opengl.GL43C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL31C.glDrawElementsInstanced;

@Mixin(VertexBuffer.class)
public abstract class VertexBufferMixin implements VertexBufferExtension {

    @Shadow
    private VertexFormat.Mode mode;

    @Shadow
    private int indexBufferId;

    @Shadow
    private int indexCount;

    @Shadow
    protected abstract VertexFormat.IndexType getIndexType();

    @Override
    public void veil$drawInstanced(int instances) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._veil$drawInstanced(instances));
        } else {
            this._veil$drawInstanced(instances);
        }
    }

    @Override
    public void veil$drawIndirect(long indirect, int drawCount, int stride) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._veil$drawIndirect(indirect, drawCount, stride));
        } else {
            this._veil$drawIndirect(indirect, drawCount, stride);
        }
    }

    @Override
    public int veil$getIndexCount() {
        return this.indexCount;
    }

    @Unique
    private void _veil$drawInstanced(int instances) {
        glDrawElementsInstanced(this.mode.asGLMode, this.indexCount, this.getIndexType().asGLType, 0L, instances);
    }

    @Unique
    private void _veil$drawIndirect(long indirect, int drawCount, int stride) {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indexBufferId);
        GL43C.glMultiDrawElementsIndirect(this.mode.asGLMode, this.getIndexType().asGLType, indirect, drawCount, stride);
    }
}
