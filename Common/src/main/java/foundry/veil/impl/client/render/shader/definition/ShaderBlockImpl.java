package foundry.veil.impl.client.render.shader.definition;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.definition.ShaderBlock;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL15C.glDeleteBuffers;

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

    protected ShaderBlockImpl(BiConsumer<T, ByteBuffer> serializer) {
        this.serializer = serializer;
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
    public abstract void unbind(int index);

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
