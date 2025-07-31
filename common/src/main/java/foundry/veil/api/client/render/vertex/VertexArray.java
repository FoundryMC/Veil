package foundry.veil.api.client.render.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.impl.client.render.vertex.ARBVertexArray;
import foundry.veil.impl.client.render.vertex.DSAVertexArray;
import foundry.veil.impl.client.render.vertex.LegacyVertexArray;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL40C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;
import java.util.function.IntFunction;

import static org.lwjgl.opengl.ARBDirectStateAccess.glCreateVertexArrays;
import static org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferData;
import static org.lwjgl.opengl.ARBMultiDrawIndirect.glMultiDrawElementsIndirect;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.opengl.GL31C.glDrawElementsInstanced;

/**
 * More generic alternative to {@link VertexBuffer} that uses the latest available OpenGL version.
 *
 * @author Ocelot
 */
public abstract class VertexArray implements NativeResource {

    public static final int VERTEX_BUFFER = 0;
    public static final int ELEMENT_ARRAY_BUFFER = 1;

    private static VertexArrayType vertexArrayType;

    protected final int id;
    protected final VertexArrayBuilder builder;
    protected final Int2IntMap buffers;
    protected int indexCount;
    protected IndexType indexType;
    protected VertexFormat.Mode drawMode;

    @ApiStatus.Internal
    protected VertexArray(int id, Function<VertexArray, VertexArrayBuilder> builder) {
        this.id = id;
        this.builder = builder.apply(this);
        this.buffers = new Int2IntArrayMap();
        this.indexCount = 0;
        this.indexType = IndexType.BYTE;
        this.drawMode = VertexFormat.Mode.TRIANGLES;
    }

    private static void loadType() {
        if (vertexArrayType == null) {
            if (VeilRenderSystem.directStateAccessSupported()) {
                vertexArrayType = VertexArrayType.DSA;
            } else {
                GLCapabilities caps = GL.getCapabilities();
                if (caps.OpenGL43 || caps.GL_ARB_vertex_attrib_binding) {
                    vertexArrayType = VertexArrayType.ARB;
                } else {
                    vertexArrayType = VertexArrayType.LEGACY;
                }
            }
        }
    }

    /**
     * Creates a single new vertex array.
     *
     * @return A new vertex array
     */
    public static VertexArray create() {
        RenderSystem.assertOnRenderThreadOrInit();
        loadType();
        return vertexArrayType.factory.apply(VeilRenderSystem.directStateAccessSupported() ? glCreateVertexArrays() : glGenVertexArrays());
    }

    /**
     * Creates an array of vertex arrays.
     *
     * @param count The number of arrays to create
     * @return An array of new vertex arrays
     */
    public static VertexArray[] create(int count) {
        VertexArray[] fill = new VertexArray[count];
        create(fill);
        return fill;
    }

    /**
     * Replaces each element of the specified array with a new vertex array.
     *
     * @param fill The array to fill
     */
    public static void create(VertexArray[] fill) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (fill.length == 0) {
            return;
        }

        loadType();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer arrays = stack.mallocInt(fill.length);
            if (VeilRenderSystem.directStateAccessSupported()) {
                glCreateVertexArrays(arrays);
            } else {
                glGenVertexArrays(arrays);
            }

            for (int i = 0; i < arrays.limit(); i++) {
                fill[i] = vertexArrayType.factory.apply(arrays.get(i));
            }
        }
    }

    /**
     * Sets up the draw state with the specified render type.
     *
     * @param renderType The render type to set up
     * @since 1.2.0
     */
    public void setup(RenderType renderType) {
        renderType.setupRenderState();
        ShaderInstance shader = RenderSystem.getShader();
        if (shader != null) {
            shader.setDefaultUniforms(this.drawMode, RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.apply();
        }
    }

    /**
     * Clears the specified render type.
     *
     * @param renderType The render type to clear
     * @since 1.2.0
     */
    public void clear(RenderType renderType) {
        ShaderInstance shader = RenderSystem.getShader();
        if (shader != null) {
            shader.clear();
        }
        renderType.clearRenderState();
    }

    /**
     * Creates a new buffer object owned by this vertex array or retrieves an existing buffer.
     *
     * @param index The index of the buffer to get
     * @return A vertex array object
     */
    public int getOrCreateBuffer(int index) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return this.buffers.computeIfAbsent(index, unused -> GlStateManager._glGenBuffers());
    }

    /**
     * @return The OpenGL id of this vertex array
     */
    public int getId() {
        return this.id;
    }

    /**
     * @return The number of indices in this array
     */
    public int getIndexCount() {
        return this.indexCount;
    }

    /**
     * @return The data type of the index buffer
     */
    public IndexType getIndexType() {
        return this.indexType;
    }

    /**
     * @return The GL polygon draw type
     * @see #setDrawMode(VertexFormat.Mode)
     */
    public VertexFormat.Mode getDrawMode() {
        return this.drawMode;
    }

    /**
     * Uploads mesh data into the specified buffer.
     *
     * @param data  The data to upload
     * @param usage The draw usage
     */
    public static void upload(int buffer, ByteBuffer data, DrawUsage usage) {
        if (VeilRenderSystem.directStateAccessSupported()) {
            glNamedBufferData(buffer, data, usage.getGlType());
        } else {
            glBindBuffer(GL_ARRAY_BUFFER, buffer);
            glBufferData(GL_ARRAY_BUFFER, data, usage.getGlType());
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
    }

    /**
     * Uploads vanilla mc mesh data into this vertex array. Only a single mesh can be uploaded this way.
     *
     * @param meshData The data to upload
     * @param usage    The draw usage
     */
    public void upload(MeshData meshData, DrawUsage usage) {
        this.upload(0, meshData, usage);
    }

    /**
     * Uploads vanilla mc mesh data into this vertex array. Only a single mesh can be uploaded this way.
     *
     * @param attributeStart The attribute to start uploading vertex data to
     * @param meshData       The data to upload
     * @param usage          The draw usage
     */
    public void upload(int attributeStart, MeshData meshData, DrawUsage usage) {
        try (meshData) {
            RenderSystem.assertOnRenderThread();
            MeshData.DrawState drawState = meshData.drawState();
            VertexArrayBuilder builder = this.editFormat();

            int vertexBuffer = this.getOrCreateBuffer(VERTEX_BUFFER);
            upload(vertexBuffer, meshData.vertexBuffer(), usage);
            builder.applyFrom(VERTEX_BUFFER, vertexBuffer, attributeStart, drawState.format());

            ByteBuffer indexBuffer = meshData.indexBuffer();
            if (indexBuffer != null) {
                this.uploadIndexBuffer(indexBuffer);
            } else {
                this.uploadIndexBuffer(drawState);
            }

            this.indexCount = drawState.indexCount();
            this.indexType = IndexType.fromBlaze3D(drawState.indexType());
            this.drawMode = drawState.mode();
        }
    }

    /**
     * Uploads index data to the vertex array.
     *
     * @param drawState The buffer draw state
     */
    public void uploadIndexBuffer(MeshData.DrawState drawState) {
        RenderSystem.getSequentialBuffer(drawState.mode()).bind(drawState.indexCount());
    }

    /**
     * Uploads index data to the vertex array.
     *
     * @param data The data to upload
     */
    public void uploadIndexBuffer(ByteBuffer data) {
        GlStateManager._glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.getOrCreateBuffer(ELEMENT_ARRAY_BUFFER));
        RenderSystem.glBufferData(GL_ELEMENT_ARRAY_BUFFER, data, GL_STATIC_DRAW);
    }

    /**
     * Uploads index data to the vertex array.
     *
     * @param data      The data to upload
     * @param indexType The type of data stored in data
     * @since 1.2.0
     */
    public void uploadIndexBuffer(ByteBuffer data, IndexType indexType) {
        this.uploadIndexBuffer(data);
        this.setIndexCount(data.remaining() >> indexType.ordinal(), indexType);
    }

    /**
     * @return A builder for applying changes to this array
     */
    public VertexArrayBuilder editFormat() {
        this.bind();
        return this.builder;
    }

    /**
     * Binds this vertex array and applies any changes to the format automatically.
     */
    public void bind() {
        VeilRenderSystem.bindVertexArray(this.id);
    }

    /**
     * Unbinds the current vertex array.
     */
    public static void unbind() {
        VeilRenderSystem.bindVertexArray(0);
    }

    /**
     * Draws {@link #indexCount} number of indices with the previously defined draw mode.
     * <br>
     * {@link #bind()} must be called before this.
     */
    public void draw() {
        glDrawElements(this.drawMode.asGLMode, this.indexCount, this.indexType.getGlType(), 0L);
    }

    /**
     * Draws {@link #indexCount} number of indices with the previously defined draw mode a number of times.
     * <br>
     * {@link #bind()} must be called before this.
     *
     * @param instances The number of instances to draw
     */
    public void drawInstanced(int instances) {
        glDrawElementsInstanced(this.drawMode.asGLMode, this.indexCount, this.indexType.getGlType(), 0L, instances);
    }

    /**
     * Draws {@link #indexCount} number of indices with the previously defined draw mode a number of times.
     * <br>
     * {@link #bind()} must be called before this.
     * <br>
     * <strong>Note: This only works if {@link VeilRenderSystem#multiDrawIndirectSupported()} is <code>true</code></strong>
     *
     * @param indirect  A pointer into the currently bound {@link GL40C#GL_DRAW_INDIRECT_BUFFER} or the address of a struct containing draw data
     * @param drawCount The number of instances to draw
     * @param stride    The stride between commands or <code>0</code> if they are tightly packed
     */
    public void drawIndirect(long indirect, int drawCount, int stride) {
        if (!VeilRenderSystem.multiDrawIndirectSupported()) {
            throw new UnsupportedOperationException("Indirect rendering is not supported");
        }

        glMultiDrawElementsIndirect(this.drawMode.asGLMode, this.indexType.getGlType(), indirect, drawCount, stride);
    }

    /**
     * Draws {@link #indexCount} number of indices with the previously defined draw mode.
     * This method applies the specified render type automatically.
     * <br>
     * {@link #bind()} must be called before this.
     */
    public void drawWithRenderType(RenderType renderType) {
        while (renderType instanceof VeilRenderType.RenderTypeWrapper wrapper) {
            renderType = wrapper.get();
        }

        if (renderType == null) {
            return;
        }

        this.setup(renderType);
        this.draw();
        this.clear(renderType);

        if (renderType instanceof VeilRenderType.LayeredRenderType layeredRenderType) {
            for (RenderType layer : layeredRenderType.getLayers()) {
                this.setup(layer);
                this.draw();
                this.clear(layer);
            }
        }
    }

    /**
     * Draws {@link #indexCount} number of indices with the previously defined draw mode a number of times.
     * This method applies the specified render type automatically.
     * <br>
     * {@link #bind()} must be called before this.
     *
     * @param instances The number of instances to draw
     */
    public void drawInstancedWithRenderType(RenderType renderType, int instances) {
        while (renderType instanceof VeilRenderType.RenderTypeWrapper wrapper) {
            renderType = wrapper.get();
        }

        if (renderType == null) {
            return;
        }

        this.setup(renderType);
        this.drawInstanced(instances);
        this.clear(renderType);

        if (renderType instanceof VeilRenderType.LayeredRenderType layeredRenderType) {
            for (RenderType layer : layeredRenderType.getLayers()) {
                this.setup(layer);
                this.drawInstanced(instances);
                this.clear(layer);
            }
        }
    }

    /**
     * Draws {@link #indexCount} number of indices with the previously defined draw mode a number of times.
     * This method applies the specified render type automatically.
     * <br>
     * {@link #bind()} must be called before this.
     * <br>
     * <strong>Note: This only works if {@link VeilRenderSystem#multiDrawIndirectSupported()} is <code>true</code></strong>
     *
     * @param indirect  A pointer into the currently bound {@link GL40C#GL_DRAW_INDIRECT_BUFFER} or the address of a struct containing draw data
     * @param drawCount The number of instances to draw
     * @param stride    The stride between commands or <code>0</code> if they are tightly packed
     */
    public void drawIndirectWithRenderType(RenderType renderType, long indirect, int drawCount, int stride) {
        while (renderType instanceof VeilRenderType.RenderTypeWrapper wrapper) {
            renderType = wrapper.get();
        }

        if (renderType == null) {
            return;
        }

        this.setup(renderType);
        this.drawIndirect(indirect, drawCount, stride);
        this.clear(renderType);

        if (renderType instanceof VeilRenderType.LayeredRenderType layeredRenderType) {
            for (RenderType layer : layeredRenderType.getLayers()) {
                this.setup(layer);
                this.drawIndirect(indirect, drawCount, stride);
                this.clear(layer);
            }
        }
    }

    /**
     * Sets the number of indices and what data type they are.
     *
     * @param indexCount The number of indices in the entire mesh
     * @param indexType  The data type of the indices
     */
    public void setIndexCount(int indexCount, IndexType indexType) {
        this.indexCount = indexCount;
        this.indexType = indexType;
    }

    /**
     * Sets the type of polygons draw calls will draw.
     *
     * @param drawMode The new draw mode
     */
    public void setDrawMode(VertexFormat.Mode drawMode) {
        this.drawMode = drawMode;
    }

    @Override
    public void free() {
        RenderSystem.assertOnRenderThreadOrInit();
        glDeleteBuffers(this.buffers.values().toIntArray());
        glDeleteVertexArrays(this.id);
        this.buffers.clear();
    }

    private enum VertexArrayType {
        LEGACY(LegacyVertexArray::new),
        ARB(ARBVertexArray::new),
        DSA(DSAVertexArray::new);

        private final IntFunction<VertexArray> factory;

        VertexArrayType(IntFunction<VertexArray> factory) {
            this.factory = factory;
        }
    }

    /**
     * The type of GL indices that can be used.
     *
     * @author Ocelot
     */
    public enum IndexType {
        BYTE(GL_UNSIGNED_BYTE),
        SHORT(GL_UNSIGNED_SHORT),
        INT(GL_UNSIGNED_INT);

        private final int glType;
        private final int bytes;

        IndexType(int glType) {
            this.glType = glType;
            this.bytes = 1 << this.ordinal();
        }

        public int getGlType() {
            return this.glType;
        }

        public int getBytes() {
            return this.bytes;
        }

        public static IndexType fromBlaze3D(VertexFormat.IndexType type) {
            return switch (type) {
                case SHORT -> SHORT;
                case INT -> INT;
            };
        }

        public static IndexType least(int maxIndex) {
            if ((maxIndex & 0xFFFFFF00) == 0) {
                return BYTE;
            }
            if ((maxIndex & 0xFFFF0000) == 0) {
                return SHORT;
            }
            return INT;
        }
    }

    /**
     * Specifies how the graphics card should manage buffer data.
     *
     * @author Ocelot
     */
    public enum DrawUsage {
        /**
         * The data is set only once and used many times.
         */
        STATIC(GL_STATIC_DRAW),
        /**
         * The data is changed a lot and used many times.
         */
        DYNAMIC(GL_DYNAMIC_DRAW),
        /**
         * The data is set only once and used by the GPU at most a few times.
         */
        STREAM(GL_STREAM_DRAW);

        private final int glType;

        DrawUsage(int glType) {
            this.glType = glType;
        }

        public int getGlType() {
            return this.glType;
        }

        /**
         * Converts the given Blaze3D type to Veil draw usage.
         *
         * @param type The type to convert
         * @return The Veil draw usage
         */
        public static DrawUsage fromBlaze3D(VertexBuffer.Usage type) {
            return switch (type) {
                case STATIC -> STATIC;
                case DYNAMIC -> DYNAMIC;
            };
        }
    }
}
