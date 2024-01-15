package foundry.veil.mixin.client.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.ext.VertexBufferExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static org.lwjgl.opengl.GL31C.glDrawElementsInstanced;

@Mixin(VertexBuffer.class)
public abstract class VertexBufferMixin implements VertexBufferExtension {

    @Shadow
    private VertexFormat.Mode mode;

    @Shadow
    private int indexCount;

    @Shadow
    protected abstract VertexFormat.IndexType getIndexType();

    @Override
    public void veil$drawInstanced(int instances) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._drawInstanced(instances));
        } else {
            this._drawInstanced(instances);
        }
    }

    @Unique
    private void _drawInstanced(int instances) {
        glDrawElementsInstanced(this.mode.asGLMode, this.indexCount, this.getIndexType().asGLType, 0L, instances);
    }
}
