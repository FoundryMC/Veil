package foundry.veil.impl.client.render.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.vertex.VertexArray;
import foundry.veil.api.client.render.vertex.VertexArrayBuilder;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

import static org.lwjgl.opengl.ARBVertexAttrib64Bit.glVertexAttribLPointer;
import static org.lwjgl.opengl.GL33C.*;

@ApiStatus.Internal
public class LegacyVertexAttribBindingBuilder implements VertexArrayBuilder {

    private final VertexArray vertexArray;
    private final VertexBufferRegion[] vertexBuffers;
    private final VertexAttribute[] vertexAttributes;
    private int boundIndex = -1;

    public LegacyVertexAttribBindingBuilder(VertexArray vertexArray) {
        this.vertexArray = vertexArray;
        this.vertexBuffers = new VertexBufferRegion[VeilRenderSystem.maxVertexAttributes()];
        this.vertexAttributes = new VertexAttribute[VeilRenderSystem.maxVertexAttributes()];
    }

    private void bindIndex(int index) {
        if (index < 0 || index >= this.vertexBuffers.length) {
            throw new IllegalArgumentException("Invalid vertex attribute index. Must be between 0 and " + (this.vertexBuffers.length - 1) + ": " + index);
        }

        if (this.boundIndex != index) {
            if (this.vertexBuffers[index] == null) {
                throw new IllegalArgumentException("No vertex buffer defined for index: " + index);
            }

            RenderSystem.glBindBuffer(GL_ARRAY_BUFFER, this.vertexBuffers[index].buffer);
            this.boundIndex = index;
        }
    }

    private void setAttribute(int index, VertexAttribute attribute) {
        this.vertexAttributes[index] = attribute;
        VertexBufferRegion buffer = this.vertexBuffers[attribute.bufferIndex()];
        if (buffer != null) {
            this.bindIndex(attribute.bufferIndex());
            attribute.apply(index, buffer);
        }
    }

    @Override
    public VertexArray vertexArray() {
        return this.vertexArray;
    }

    @Override
    public VertexArrayBuilder defineVertexBuffer(int index, int buffer, int offset, int stride, int divisor) {
        if (index < 0 || index >= this.vertexBuffers.length) {
            throw new IllegalArgumentException("Invalid vertex attribute index. Must be between 0 and " + (this.vertexBuffers.length - 1) + ": " + index);
        }
        this.vertexBuffers[index] = new VertexBufferRegion(buffer, offset, stride, divisor);
        this.bindIndex(index);
        for (VertexAttribute attribute : this.vertexAttributes) {
            if (attribute != null && attribute.bufferIndex() == index) {
                attribute.apply(index, this.vertexBuffers[index]);
            }
        }
        return this;
    }

    @Override
    public VertexArrayBuilder setVertexAttribute(int index, int bufferIndex, int size, DataType type, boolean normalized, int relativeOffset) {
        VertexArrayBuilder.validateFloatType(type, size);
        VertexArrayBuilder.validateRelativeOffset(relativeOffset);
        glEnableVertexAttribArray(index);
        this.setAttribute(index, new FloatAttribute(bufferIndex, size, type, normalized, relativeOffset));
        return this;
    }

    @Override
    public VertexArrayBuilder setVertexIAttribute(int index, int bufferIndex, int size, DataType type, int relativeOffset) {
        VertexArrayBuilder.validateIntType(type);
        VertexArrayBuilder.validateRelativeOffset(relativeOffset);
        glEnableVertexAttribArray(index);
        this.setAttribute(index, new IntAttribute(bufferIndex, size, type, relativeOffset));
        return this;
    }

    @Override
    public VertexArrayBuilder setVertexLAttribute(int index, int bufferIndex, int size, DataType type, int relativeOffset) {
        VertexArrayBuilder.validateLongType(type);
        VertexArrayBuilder.validateRelativeOffset(relativeOffset);
        glEnableVertexAttribArray(index);
        this.setAttribute(index, new LongAttribute(bufferIndex, size, type, relativeOffset));
        return this;
    }

    @Override
    public VertexArrayBuilder removeVertexBuffer(int index) {
        if (index < 0 || index >= this.vertexBuffers.length) {
            throw new IllegalArgumentException("Invalid vertex attribute index. Must be between 0 and " + (this.vertexBuffers.length - 1) + ": " + index);
        }
        this.vertexBuffers[index] = null;
        return this;
    }

    @Override
    public VertexArrayBuilder removeAttribute(int index) {
        glDisableVertexAttribArray(index);
        this.vertexAttributes[index] = null;
        return this;
    }

    @Override
    public VertexArrayBuilder clearVertexBuffers() {
        Arrays.fill(this.vertexBuffers, null);
        return this;
    }

    @Override
    public VertexArrayBuilder clearVertexAttributes() {
        for (int i = 0; i < this.vertexAttributes.length; i++) {
            glDisableVertexAttribArray(i);
        }
        Arrays.fill(this.vertexAttributes, null);
        return this;
    }

    private sealed interface VertexAttribute {
        int bufferIndex();

        void apply(int index, VertexBufferRegion region);
    }

    private record FloatAttribute(int bufferIndex, int size, DataType type, boolean normalized, int relativeOffset) implements VertexAttribute {
        @Override
        public void apply(int index, VertexBufferRegion region) {
            glVertexAttribPointer(index, this.size, this.type.getGlType(), this.normalized, region.stride, region.offset + this.relativeOffset);
            glVertexAttribDivisor(index, region.divisor);
        }
    }

    private record IntAttribute(int bufferIndex, int size, DataType type, int relativeOffset) implements VertexAttribute {
        @Override
        public void apply(int index, VertexBufferRegion region) {
            glVertexAttribIPointer(index, this.size, this.type.getGlType(), region.stride, region.offset + this.relativeOffset);
            glVertexAttribDivisor(index, region.divisor);
        }
    }

    private record LongAttribute(int bufferIndex, int size, DataType type, int relativeOffset) implements VertexAttribute {
        @Override
        public void apply(int index, VertexBufferRegion region) {
            glVertexAttribLPointer(index, this.size, this.type.getGlType(), region.stride, region.offset + this.relativeOffset);
            glVertexAttribDivisor(index, region.divisor);
        }
    }

    private record VertexBufferRegion(int buffer, int offset, int stride, int divisor) {
    }
}
