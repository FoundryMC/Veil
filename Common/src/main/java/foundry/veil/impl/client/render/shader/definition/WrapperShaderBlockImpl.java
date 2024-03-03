package foundry.veil.impl.client.render.shader.definition;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.definition.DynamicShaderBlock;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import static org.lwjgl.opengl.GL30C.glBindBufferRange;

@ApiStatus.Internal
public class WrapperShaderBlockImpl extends ShaderBlockImpl<Object> implements DynamicShaderBlock<Object> {

    private long size;

    public WrapperShaderBlockImpl(int binding, int buffer, int size) {
        super(binding);
        this.buffer = buffer;
        this.setSize(size);
    }

    @Override
    public void bind(int index) {
        Validate.inclusiveBetween(0, VeilRenderSystem.maxTargetBindings(this.binding), index);
        glBindBufferRange(this.binding, index, this.buffer, 0, this.size);
    }

    @Override
    public void unbind(int index) {
        Validate.inclusiveBetween(0, VeilRenderSystem.maxTargetBindings(this.binding), index);
        glBindBufferRange(this.binding, index, 0, 0, this.size);
    }

    @Override
    public void set(@Nullable Object value) {
        throw new UnsupportedOperationException("Buffer Shader Block cannot be set to a java object");
    }

    @Override
    public @Nullable Object getValue() {
        throw new UnsupportedOperationException("Buffer Shader Block cannot be read as a java object");
    }

    @Override
    public void setSize(long newSize) {
        this.size = newSize;
    }

    @Override
    public void free() {
        VeilRenderSystem.unbind(this);
    }
}
