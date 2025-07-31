package foundry.veil.api.client.render.vertex;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import foundry.veil.api.client.render.VeilRenderSystem;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

import static org.lwjgl.opengl.ARBBindlessTexture.GL_UNSIGNED_INT64_ARB;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.opengl.GL41C.GL_FIXED;

/**
 * Builder for modifying vertex buffer state.
 *
 * @author Ocelot
 */
public interface VertexArrayBuilder {

    @ApiStatus.Internal
    static void validateRelativeOffset(int offset) {
        if (offset < 0 || offset > VeilRenderSystem.maxVertexAttributeRelativeOffset()) {
            throw new IllegalArgumentException("Vertex array attribute relative offset must be between 0 and " + VeilRenderSystem.maxVertexAttributeRelativeOffset() + ". Was " + offset);
        }
    }

    @ApiStatus.Internal
    static void validateFloatType(DataType type, int size) {
        if (!type.isSupported()) {
            throw new UnsupportedOperationException(type + " attributes not supported");
        }
        if (type == DataType.UNSIGNED_INT_10F_11F_11F_REV && size != 3) {
            throw new IllegalArgumentException("Invalid vertex attribute size. Must be 3: " + size);
        }
        if ((type == DataType.INT_2_10_10_10_REV || type == DataType.UNSIGNED_INT_2_10_10_10_REV) && (size != 4 && size != GL_BGRA)) {
            throw new IllegalArgumentException("Invalid vertex attribute size. Must be 4 or GL_BGRA: " + size);
        }
    }

    @ApiStatus.Internal
    static void validateIntType(DataType type) {
        if (!type.isSupported()) {
            throw new UnsupportedOperationException(type + " attributes not supported");
        }
    }

    @ApiStatus.Internal
    static void validateLongType(DataType type) {
        if (!type.isSupported()) {
            throw new UnsupportedOperationException(type + " attributes not supported");
        }
        if (!VeilRenderSystem.vertexAttribute64BitSupported()) {
            throw new UnsupportedOperationException("Long attributes not supported");
        }
    }

    /**
     * @return The source vertex array
     */
    VertexArray vertexArray();

    /**
     * Applies the vanilla mc format at the specified index.
     *
     * @param bufferIndex    The index to map the buffer to
     * @param buffer         The buffer to get data from
     * @param attributeStart The first attribute index to start applying the format from
     * @param format         The format to apply
     */
    default VertexArrayBuilder applyFrom(int bufferIndex, int buffer, int attributeStart, VertexFormat format) {
        this.defineVertexBuffer(bufferIndex, buffer, 0, format.getVertexSize(), 0);
        List<VertexFormatElement> elements = format.getElements();
        for (int i = 0; i < elements.size(); i++) {
            VertexFormatElement element = elements.get(i);
            VertexFormatElement.Usage usage = element.usage();

            if (usage == VertexFormatElement.Usage.UV && element.type() != VertexFormatElement.Type.FLOAT) {
                this.setVertexIAttribute(
                        attributeStart + i,
                        bufferIndex,
                        element.count(),
                        DataType.fromType(element.type()),
                        format.getOffset(element)
                );
            } else {
                this.setVertexAttribute(
                        attributeStart + i,
                        bufferIndex,
                        element.count(),
                        DataType.fromType(element.type()),
                        usage == VertexFormatElement.Usage.NORMAL || usage == VertexFormatElement.Usage.COLOR,
                        format.getOffset(element)
                );
            }
        }
        return this;
    }

    /**
     * Maps a buffer region to the specified index. Allows swapping out vertex data with a single GL call.
     *
     * @param index   The index to assign to. It must be between 0 and {@link VeilRenderSystem#maxVertexAttributes()}
     * @param buffer  The buffer to assign
     * @param offset  The offset into the buffer to bind to
     * @param stride  The size of the region to map
     * @param divisor The number of instances that have to pass to increment this data or <code>0</code> to increment per vertex
     */
    VertexArrayBuilder defineVertexBuffer(int index, int buffer, int offset, int stride, int divisor);

    /**
     * Defines a floating-point vertex attribute.
     *
     * @param index          The index of the attribute to assign
     * @param bufferIndex    The defined buffer index to use. Defined with {@link VertexArrayBuilder#defineVertexBuffer}
     * @param size           The size of the attribute. Can be 1, 2, 3, or 4
     * @param type           The type of data the shader will use
     * @param normalized     Whether to normalize the data from <code>-1</code> to <code>1</code> automatically
     * @param relativeOffset The offset in the buffer region the vertex data is defined at. It must be between 0 and {@link VeilRenderSystem#maxVertexAttributeRelativeOffset()}
     */
    VertexArrayBuilder setVertexAttribute(int index, int bufferIndex, int size, DataType type, boolean normalized, int relativeOffset);

    /**
     * Defines an integer vertex attribute.
     *
     * @param index          The index of the attribute to assign
     * @param bufferIndex    The defined buffer index to use. Defined with {@link VertexArrayBuilder#defineVertexBuffer}
     * @param size           The size of the attribute. Can be 1, 2, 3, or 4
     * @param type           The type of data the shader will use
     * @param relativeOffset The offset in the buffer region the vertex data is defined at. It must be between 0 and {@link VeilRenderSystem#maxVertexAttributeRelativeOffset()}
     */
    VertexArrayBuilder setVertexIAttribute(int index, int bufferIndex, int size, DataType type, int relativeOffset);

    /**
     * Defines a long vertex attribute. Only supported if {@link VeilRenderSystem#vertexAttribute64BitSupported()} or {@link VeilRenderSystem#bindlessTextureSupported()} are <code>true</code>.
     *
     * @param index          The index of the attribute to assign
     * @param bufferIndex    The defined buffer index to use. Defined with {@link VertexArrayBuilder#defineVertexBuffer}
     * @param size           The size of the attribute. Can be 1, 2, 3, or 4
     * @param type           The type of data the shader will use
     * @param relativeOffset The offset in the buffer region the vertex data is defined at. It must be between 0 and {@link VeilRenderSystem#maxVertexAttributeRelativeOffset()}
     */
    VertexArrayBuilder setVertexLAttribute(int index, int bufferIndex, int size, DataType type, int relativeOffset);

    /**
     * Removes the buffer mapping with the specified index.
     *
     * @param index The index of the buffer to remove
     */
    VertexArrayBuilder removeVertexBuffer(int index);

    /**
     * Removes the attribute with the specified index.
     *
     * @param index The index of the attribute to remove
     */
    VertexArrayBuilder removeAttribute(int index);

    /**
     * Clears all mapped buffer regions.
     */
    VertexArrayBuilder clearVertexBuffers();

    /**
     * Clears all defined vertex attributes.
     */
    VertexArrayBuilder clearVertexAttributes();

    /**
     * Possible data type for vertex attributes.
     */
    enum DataType {
        BYTE(GL_BYTE),
        SHORT(GL_SHORT),
        INT(GL_INT),
        FIXED(GL_FIXED),
        FLOAT(GL_FLOAT),
        HALF_FLOAT(GL_HALF_FLOAT),
        DOUBLE(GL_DOUBLE),

        UNSIGNED_BYTE(GL_UNSIGNED_BYTE),
        UNSIGNED_SHORT(GL_UNSIGNED_SHORT),
        UNSIGNED_INT(GL_UNSIGNED_INT),

        INT_2_10_10_10_REV(GL_INT_2_10_10_10_REV),
        UNSIGNED_INT_2_10_10_10_REV(GL_UNSIGNED_INT_2_10_10_10_REV),

        // ARB_vertex_type_10f_11f_11f_rev
        UNSIGNED_INT_10F_11F_11F_REV(GL_UNSIGNED_INT_10F_11F_11F_REV),

        // ARBBindlessTexture
        /**
         * @since 2.0.0
         */
        UNSIGNED_INT64_ARB(GL_UNSIGNED_INT64_ARB);

        private final int glType;

        DataType(int glType) {
            this.glType = glType;
        }

        /**
         * Checks if this data type is supported on this platform.
         * @since 2.0.0
         * @return Whether this data type is supported
         */
        public boolean isSupported() {
            if (this == UNSIGNED_INT_10F_11F_11F_REV) {
                return VeilRenderSystem.vertexType10F11F11FRevSupported();
            } else if (this == UNSIGNED_INT64_ARB) {
                return VeilRenderSystem.bindlessTextureSupported();
            }
            return true;
        }

        /**
         * @return The OpenGL data type of this type
         */
        public int getGlType() {
            return this.glType;
        }

        /**
         * Converts a Mojang type to this type.
         * @param type The type to convert
         * @return The associated type
         */
        public static DataType fromType(VertexFormatElement.Type type) {
            return switch (type) {
                case FLOAT -> FLOAT;
                case UBYTE -> UNSIGNED_BYTE;
                case BYTE -> BYTE;
                case USHORT -> UNSIGNED_SHORT;
                case SHORT -> SHORT;
                case UINT -> UNSIGNED_INT;
                case INT -> INT;
            };
        }
    }
}
