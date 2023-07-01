package foundry.veil.shader.definition;

import foundry.veil.pipeline.VeilRenderSystem;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.glBindBufferRange;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;

/**
 * Dynamic-size implementation of {@link ShaderBlock}.
 *
 * @param <T> The type of object to serialize
 * @author Ocelot
 */
@ApiStatus.Internal
public class DynamicShaderBlockImpl<T> extends ShaderBlockImpl<T> implements DynamicShaderBlock<T> {

    private int size;
    private boolean resized;

    /**
     * Creates a new shader block of specified initial size.
     *
     * @param size       The initial size of the shader buffer
     * @param serializer The serializer of values
     */
    DynamicShaderBlockImpl(int size, @NotNull BiConsumer<T, ByteBuffer> serializer) {
        super(serializer);
        this.size = size;
        this.resized = false;
    }

    @Override
    public void setSize(int size) {
        this.size = size;
        this.resized = true;
    }

    @Override
    public void bind(int index) {
        Validate.inclusiveBetween(0, VeilRenderSystem.maxUniformBuffersBindings(), index);

        if (this.buffer == 0) {
            this.buffer = glGenBuffers();
            this.resized = true;
        }

        if (this.resized) {
            this.resized = false;
            this.dirty = true;
            glBindBuffer(GL_UNIFORM_BUFFER, this.buffer);
            glBufferData(GL_UNIFORM_BUFFER, this.size, GL_DYNAMIC_DRAW);
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
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
}
