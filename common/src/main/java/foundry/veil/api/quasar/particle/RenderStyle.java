package foundry.veil.api.quasar.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.serialization.Codec;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.vertex.VeilVertexFormat;
import foundry.veil.api.client.render.vertex.VertexArray;
import foundry.veil.api.client.render.vertex.VertexArrayBuilder;
import foundry.veil.api.quasar.registry.RenderStyleRegistry;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.joml.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.system.MemoryUtil.memAddress;

/**
 * Defines how a particle emitter renders a set of particles.
 *
 * @author Ocelot, BL
 */
public abstract class RenderStyle implements NativeResource {

    public static final Codec<RenderStyle> CODEC = CodecUtil.registryOrLegacyCodec(RenderStyleRegistry.REGISTRY);

    private VertexArray vertexArray;
    private int instanceVBO;
    private final int bufferSize;
    protected int maxParticles;

    public static final int MIN_PARTICLES = 400;

    public RenderStyle(int bufferSize) {
        this.bufferSize = bufferSize;
        this.maxParticles = 0;
    }

    /**
     * Called after the Veil renderer is available, sets up anything necessary for rendering.
     */
    public void init() {
        if (bufferSize <= 0) {
            return;
        }

        this.vertexArray = VertexArray.create();
        this.vertexArray.upload(this.createMesh(), VertexArray.DrawUsage.STATIC);
        this.instanceVBO = this.vertexArray.getOrCreateBuffer(2);

        VertexArrayBuilder builder = this.vertexArray.editFormat();
        builder.defineVertexBuffer(2, this.instanceVBO, 0, bufferSize, 1);
        this.setupBufferState(builder);
    }

    /**
     * Called before rendering any particles. This will not be fired if the emitter has no particles.
     *
     * @param particleCount The number of particles that will be rendered with {@link #render(List, Camera)}
     * @return Whether the particles are allowed to render
     * @since 1.3.0
     */
    public boolean setup(int particleCount) {
        return true;
    }

    /**
     * Called after rendering all particles.
     *
     * @since 1.3.0
     */
    public void clear() {
    }

    /**
     * Draws a list of particles using a batched VertexArray.
     *
     * @param particles    A list of currently active particles
     * @param camera       The camera to use to render the particles
     */
    public void render(List<QuasarParticle> particles, Camera camera) {
        if (particles.isEmpty()) return;

        RenderType renderType = particles.getFirst().getRenderData().getRenderType();
        if (renderType == null) {
            return;
        }

        // Perform frustum culling for each particle
        List<QuasarParticle> visibleParticles = particles.stream().filter(particle -> VeilRenderSystem.getCullingFrustum().testSphere(particle.getPosition(), particle.getRenderData().getRenderRadius())).toList();
        if (visibleParticles.isEmpty()) return;

        RenderSystem.glBindBuffer(GL_ARRAY_BUFFER, this.instanceVBO);

        if (visibleParticles.size() > this.maxParticles) {
            if (this.maxParticles < 100) {
                this.maxParticles = 100;
            } else {
                this.maxParticles = (int) Math.max(Math.ceil(this.maxParticles / 2.0), visibleParticles.size() * 1.5);
            }
            glBufferData(GL_ARRAY_BUFFER, (long) this.maxParticles * this.bufferSize, GL_STATIC_DRAW);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            int pointer = 0;
            long offset = 0;
            ByteBuffer dataBuffer = stack.malloc(Math.min(MIN_PARTICLES, visibleParticles.size()) * this.bufferSize);
            for (QuasarParticle particle : visibleParticles) {
                dataBuffer.position((pointer++) * this.bufferSize);

                this.putBufferData(particle, camera, dataBuffer);

                if (pointer >= MIN_PARTICLES) {
                    dataBuffer.rewind();
                    glBufferSubData(GL_ARRAY_BUFFER, offset, dataBuffer);
                    offset += dataBuffer.capacity();
                    pointer = 0;
                }
            }

            if (pointer > 0) {
                dataBuffer.rewind();
                nglBufferSubData(GL_ARRAY_BUFFER, offset, (long) pointer * this.bufferSize, memAddress(dataBuffer));
            }
        }

        this.vertexArray.bind();
        this.vertexArray.drawInstancedWithRenderType(renderType, visibleParticles.size());
    }

    /**
     * @return The render type to use for the specified particle.
     * @apiNote This is only called when the render data is marked dirty
     * @see RenderData#markDirty()
     * @since 1.3.0
     */
    public RenderType getRenderType(QuasarParticle particle, RenderData renderData) {
        boolean additive = renderData.isAdditive();
        TextureAtlasSprite atlasSprite = renderData.getAtlasSprite();
        if (atlasSprite != null) {
            return VeilRenderType.quasarParticle(atlasSprite.atlasLocation(), additive);
        }

        SpriteData spriteData = renderData.getSpriteData();
        if (spriteData != null) {
            return VeilRenderType.quasarParticle(spriteData.sprite(), additive);
        }

        return VeilRenderType.quasarParticle(RenderData.BLANK, additive);
    }

    @Override
    public void free() {
        this.vertexArray.free();

    }

    /**
     * @return The MeshData to use for each particle.
     */
    protected abstract MeshData createMesh();

    /**
     * Set up the vertex attribute arrays to use for each particle.
     * @param builder The builder associated with the VertexArray
     */
    protected abstract void setupBufferState(VertexArrayBuilder builder);

    /**
     * Put information about each particle into the vertex attribute array.
     * @param particle The particle being loaded
     * @param camera The camera rendering the particles
     * @param buffer The buffer of the current VertexArray
     */
    protected abstract void putBufferData(QuasarParticle particle, Camera camera, ByteBuffer buffer);

    @ApiStatus.Internal
    public static final class Cube extends RenderStyle {
        private static final Vector3fc[] CUBE_POSITIONS = {
                // TOP
                new Vector3f(1, 1, -1), new Vector3f(1, 1, 1), new Vector3f(-1, 1, 1), new Vector3f(-1, 1, -1),

                // BOTTOM
                new Vector3f(-1, -1, -1), new Vector3f(-1, -1, 1), new Vector3f(1, -1, 1), new Vector3f(1, -1, -1),

                // FRONT
                new Vector3f(-1, -1, 1), new Vector3f(-1, 1, 1), new Vector3f(1, 1, 1), new Vector3f(1, -1, 1),

                // BACK
                new Vector3f(1, -1, -1), new Vector3f(1, 1, -1), new Vector3f(-1, 1, -1), new Vector3f(-1, -1, -1),

                // LEFT
                new Vector3f(-1, -1, -1), new Vector3f(-1, 1, -1), new Vector3f(-1, 1, 1), new Vector3f(-1, -1, 1),

                // RIGHT
                new Vector3f(1, -1, 1), new Vector3f(1, 1, 1), new Vector3f(1, 1, -1), new Vector3f(1, -1, -1)
        };
        private static final float[] CUBE_NORMALS = {0, 1, 0, 0, -1, 0, 0, 0, 1, 0, 0, -1, -1, 0, 0, 1, 0, 0};

        public Cube() {
            super(Float.BYTES * 25 + Short.BYTES * 2);
        }

        @Override
        protected MeshData createMesh() {
            BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, VeilVertexFormat.QUASAR_PARTICLE);

            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 4; j++) {
                    Vector3fc pos = CUBE_POSITIONS[i * 4 + j];

                    builder.addVertex(pos.x(), pos.y(), pos.z());
                    builder.setNormal(CUBE_NORMALS[i * 3], CUBE_NORMALS[i * 3 + 1], CUBE_NORMALS[i * 3 + 2]);
                }
            }

            return builder.buildOrThrow();
        }

        @Override
        protected void setupBufferState(VertexArrayBuilder builder) {
            builder. setVertexAttribute(2,  2, 4, VertexArrayBuilder.DataType.FLOAT, false, 0               ); // Model Matrix[0]
            builder. setVertexAttribute(3,  2, 4, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 4 ); // Model Matrix[1]
            builder. setVertexAttribute(4,  2, 4, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 8 ); // Model Matrix[2]
            builder. setVertexAttribute(5,  2, 4, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 12); // Model Matrix[3]
            builder. setVertexAttribute(6,  2, 1, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 16); // Scale
            builder. setVertexAttribute(7,  2, 2, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 17); // UV0 min
            builder. setVertexAttribute(8,  2, 2, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 19); // UV0 max
            builder. setVertexAttribute(9,  2, 4, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 21); // Color
            builder.setVertexIAttribute(10, 2, 2, VertexArrayBuilder.DataType.SHORT,                  Float.BYTES * 25); // UV2 / Light
        }

        @Override
        protected void putBufferData(QuasarParticle particle, Camera camera, ByteBuffer buffer) {
            RenderData renderData = particle.getRenderData();
            Vector3fc rotation = renderData.getRenderRotation();
            SpriteData spriteData = renderData.getSpriteData();

            Vec3 projectedView = camera.getPosition();
            Vector3f renderOffset = new Vector3f();
            Vector3dc renderPosition = renderData.getRenderPosition();
            renderOffset.set(
                    (float) (renderPosition.x() - projectedView.x),
                    (float) (renderPosition.y() - projectedView.y),
                    (float) (renderPosition.z() - projectedView.z));

            Matrix4f transformationMatrix = new Matrix4f()
                    .translate(renderOffset.x, renderOffset.y, renderOffset.z)
                    .rotate(new Quaternionf().rotateLocalX(rotation.x()).rotateLocalY(rotation.y()).rotateLocalZ(rotation.z()));
            transformationMatrix.get(buffer.position(), buffer);

            buffer.position(buffer.position() + Float.BYTES * 16);

            buffer.putFloat(renderData.getRenderRadius());

            float uMin = 0;
            float vMin = 0;
            float uMax = 1;
            float vMax = 1;
            if (spriteData != null) {
                uMin = spriteData.u(renderData.getRenderAge(), renderData.getAgePercent(), uMin);
                uMax = spriteData.u(renderData.getRenderAge(), renderData.getAgePercent(), uMax);
                vMin = spriteData.v(renderData.getRenderAge(), renderData.getAgePercent(), vMin);
                vMax = spriteData.v(renderData.getRenderAge(), renderData.getAgePercent(), vMax);
            }
            buffer.putFloat(uMin);
            buffer.putFloat(vMin);
            buffer.putFloat(uMax);
            buffer.putFloat(vMax);

            buffer.putFloat(renderData.getRed());
            buffer.putFloat(renderData.getGreen());
            buffer.putFloat(renderData.getBlue());
            buffer.putFloat(renderData.getAlpha());

            int packedLight = renderData.getPackedLight();
            buffer.putShort((short) ((packedLight & 65535)));
            buffer.putShort((short) ((packedLight >> 16 & 65535)));
        }
    }

    @ApiStatus.Internal
    public static final class Billboard extends RenderStyle {

        private static final Vector3fc[] PLANE_POSITIONS = {
                // plane from -1 to 1 on Y axis and -1 to 1 on X axis
                new Vector3f(1, -1, 0), new Vector3f(1, 1, 0), new Vector3f(-1, 1, 0), new Vector3f(-1, -1, 0),
        };

        public Billboard() {
            super(Float.BYTES * 25 + Short.BYTES * 2);
        }

        @Override
        protected MeshData createMesh() {
            BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, VeilVertexFormat.QUASAR_PARTICLE);

            for (int j = 0; j < 4; j++) {
                Vector3fc pos = PLANE_POSITIONS[j];

                builder.addVertex(pos.x(), pos.y(), pos.z());
                builder.setNormal(0, 0, -1);
            }

            return builder.buildOrThrow();
        }

        @Override
        protected void setupBufferState(VertexArrayBuilder builder) {
            builder. setVertexAttribute(2,  2, 4, VertexArrayBuilder.DataType.FLOAT, false, 0               ); // Model Matrix[0]
            builder. setVertexAttribute(3,  2, 4, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 4 ); // Model Matrix[1]
            builder. setVertexAttribute(4,  2, 4, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 8 ); // Model Matrix[2]
            builder. setVertexAttribute(5,  2, 4, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 12); // Model Matrix[3]
            builder. setVertexAttribute(6,  2, 1, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 16); // Scale
            builder. setVertexAttribute(7,  2, 2, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 17); // UV0 min
            builder. setVertexAttribute(8,  2, 2, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 19); // UV0 max
            builder. setVertexAttribute(9,  2, 4, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 21); // Color
            builder.setVertexIAttribute(10, 2, 2, VertexArrayBuilder.DataType.SHORT,        Float.BYTES * 25); // UV2 / Light
        }

        @Override
        protected void putBufferData(QuasarParticle particle, Camera camera, ByteBuffer buffer) {
            RenderData renderData = particle.getRenderData();
            Quaternionf faceCameraRotation = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation().get(new Quaternionf());
            Vector3fc rotation = renderData.getRenderRotation();
            SpriteData spriteData = renderData.getSpriteData();

            Vec3 projectedView = camera.getPosition();
            Vector3f renderOffset = new Vector3f();
            Vector3dc renderPosition = renderData.getRenderPosition();
            renderOffset.set(
                    (float) (renderPosition.x() - projectedView.x),
                    (float) (renderPosition.y() - projectedView.y),
                    (float) (renderPosition.z() - projectedView.z));

            Matrix4f transformationMatrix = new Matrix4f()
                    .translate(renderOffset.x, renderOffset.y, renderOffset.z)
                    .rotate(faceCameraRotation.rotateLocalX(rotation.x()).rotateLocalY(rotation.y()).rotateLocalZ(rotation.z()));
            transformationMatrix.get(buffer.position(), buffer);

            buffer.position(buffer.position() + Float.BYTES * 16);

            buffer.putFloat(renderData.getRenderRadius());

            float uMin = 0;
            float vMin = 0;
            float uMax = 1;
            float vMax = 1;
            if (spriteData != null) {
                uMin = spriteData.u(renderData.getRenderAge(), renderData.getAgePercent(), uMin);
                uMax = spriteData.u(renderData.getRenderAge(), renderData.getAgePercent(), uMax);
                vMin = spriteData.v(renderData.getRenderAge(), renderData.getAgePercent(), vMin);
                vMax = spriteData.v(renderData.getRenderAge(), renderData.getAgePercent(), vMax);
            }
            buffer.putFloat(uMin);
            buffer.putFloat(vMin);
            buffer.putFloat(uMax);
            buffer.putFloat(vMax);

            buffer.putFloat(renderData.getRed());
            buffer.putFloat(renderData.getGreen());
            buffer.putFloat(renderData.getBlue());
            buffer.putFloat(renderData.getAlpha());

            int packedLight = renderData.getPackedLight();
            buffer.putShort((short) ((packedLight & 65535)));
            buffer.putShort((short) ((packedLight >> 16 & 65535)));
        }
    }
}
