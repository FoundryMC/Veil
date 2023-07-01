package foundry.veil.shader.definition;

import foundry.veil.pipeline.VeilRenderSystem;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL30C.glBindBufferRange;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;

/**
 * Abstract implementation of {@link ShaderBlock}.
 *
 * @param <T> The type of object to serialize
 * @author Ocelot
 */
@ApiStatus.Internal
public abstract class ShaderBlockImpl<T> implements ShaderBlock<T> {

    protected final BiConsumer<T, ByteBuffer> serializer;
    protected int buffer;
    protected T value;
    protected boolean dirty;

    protected ShaderBlockImpl(@NotNull BiConsumer<T, ByteBuffer> serializer) {
        this.serializer = Objects.requireNonNull(serializer, "serializer");
        this.buffer = 0;
        this.value = null;
        this.dirty = false;
    }

    @Override
    public void set(@Nullable T value) {
        this.value = value;
        this.dirty = true;
    }

    /**
     * Binds this block to the specified index.
     *
     * @param index The index to bind this block to
     */
    public abstract void bind(int index);

    /**
     * Unbinds this block from the specified index.
     *
     * @param index The index to unbind this block from
     */
    public void unbind(int index) {
        Validate.inclusiveBetween(0, VeilRenderSystem.maxUniformBuffersBindings(), index);
        glBindBufferRange(GL_UNIFORM_BUFFER, index, this.buffer, 0, 0);
    }

    @Override
    public @Nullable T getValue() {
        return this.value;
    }

    @Override
    public void free() {
        VeilRenderSystem.unbind(this);
        if (this.buffer != 0) {
            glDeleteBuffers(this.buffer);
            this.buffer = 0;
        }
    }
}
