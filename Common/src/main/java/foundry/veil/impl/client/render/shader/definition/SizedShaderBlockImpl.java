package foundry.veil.impl.client.render.shader.definition;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.definition.ShaderBlock;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.glBindBufferRange;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;

/**
 * Fixed-size implementation of {@link ShaderBlock}.
 *
 * @param <T> The type of object to serialize
 * @author Ocelot
 */
@ApiStatus.Internal
public class SizedShaderBlockImpl<T> extends ShaderBlockImpl<T> {

    private final int size;

    /**
     * Creates a new shader block of specified size.
     *
     * @param size       The size of the shader buffer
     * @param serializer The serializer of values
     */
    public SizedShaderBlockImpl(int size, BiConsumer<T, ByteBuffer> serializer) {
        super(serializer);
        this.size = size;
    }

    @Override
    public void bind(int index) {
        Validate.inclusiveBetween(0, VeilRenderSystem.maxUniformBuffersBindings(), index);

        if (this.buffer == 0) {
            this.buffer = glGenBuffers();
            glBindBuffer(GL_UNIFORM_BUFFER, this.buffer);
            glBufferData(GL_UNIFORM_BUFFER, this.size, GL_DYNAMIC_DRAW);
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
            this.dirty = true;
        }

        if (this.dirty) {
            this.dirty = false;
            glBindBuffer(GL_UNIFORM_BUFFER, this.buffer);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                if (this.value != null) {
                    ByteBuffer buffer = stack.malloc(this.size);
                    this.serializer.accept(this.value, buffer);
                    glBufferSubData(GL_UNIFORM_BUFFER, 0, buffer);
                } else {
                    glBufferSubData(GL_UNIFORM_BUFFER, 0, stack.calloc(this.size));
                }
            }
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        glBindBufferRange(GL_UNIFORM_BUFFER, index, this.buffer, 0, this.size);
    }

    @Override
    public void unbind(int index) {
        Validate.inclusiveBetween(0, VeilRenderSystem.maxUniformBuffersBindings(), index);
        glBindBufferRange(GL_UNIFORM_BUFFER, index, this.buffer, 0, this.size);
    }
}
