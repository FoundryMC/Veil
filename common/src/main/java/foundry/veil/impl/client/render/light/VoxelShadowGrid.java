package foundry.veil.impl.client.render.light;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.renderer.DDALightRenderer;
import foundry.veil.api.client.render.light.renderer.LightTypeRenderer;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Objects;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL21C.GL_PIXEL_UNPACK_BUFFER;
import static org.lwjgl.opengl.GL30C.GL_R8;

@ApiStatus.Internal
public final class VoxelShadowGrid {

    public static final int GRID_SIZE = 64;
    private static final int HALF = GRID_SIZE >> 1;
    private static final int GRID_VOLUME = GRID_SIZE * GRID_SIZE * GRID_SIZE;
    private static final int SLICE_AREA = GRID_SIZE * GRID_SIZE;

    private static final int MAX_SLICE_UPDATES_PER_FRAME = 2;
    private static final long BUILD_BUDGET_NS = 2_000_000L;
    private static final int MAX_DIRTY_UPDATES_PER_FRAME = 512;
    private static final int MAX_DIRTY_BACKLOG = 16384;

    private static final Vector3f uniformGridPos = new Vector3f();
    private static int textureId;
    private static int pboId;

    private static ResourceKey<Level> gridDimension;
    private static int originX, originY, originZ;
    private static ByteBuffer gridBuffer;

    private static ResourceKey<Level> buildDimension;
    private static int buildOriginX, buildOriginY, buildOriginZ;
    private static int buildIndex;
    private static ByteBuffer buildBuffer;

    private static final Object DIRTY_LOCK = new Object();
    private static final LongArrayFIFOQueue DIRTY_QUEUE = new LongArrayFIFOQueue();
    private static final LongOpenHashSet DIRTY_SET = new LongOpenHashSet();
    private static final long[] DRAIN_SCRATCH = new long[MAX_DIRTY_UPDATES_PER_FRAME];
    private static boolean rebuildRequested;

    private VoxelShadowGrid() {
    }

    public static void setup() {
        RenderSystem.assertOnRenderThread();

        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) {
            return;
        }

        ensureTexture();

        if (gridDimension != null && !Objects.equals(gridDimension, level.dimension())) {
            clearLevel();
        }

        Vec3 cameraPos = client.gameRenderer.getMainCamera().getPosition();
        int cx = (int) Math.floor(cameraPos.x);
        int cy = (int) Math.floor(cameraPos.y);
        int cz = (int) Math.floor(cameraPos.z);

        if (hasOccludedLights()) {
            if (rebuildRequested) {
                rebuildRequested = false;
                clearDirty();
                startFullBuild(level, cx, cy, cz);
            } else if (buildBuffer != null) {
                int maxDelta = Math.max(
                        Math.abs(cx - (buildOriginX + HALF)),
                        Math.max(Math.abs(cy - (buildOriginY + HALF)), Math.abs(cz - (buildOriginZ + HALF)))
                );
                if (!Objects.equals(buildDimension, level.dimension()) || maxDelta >= HALF) {
                    startFullBuild(level, cx, cy, cz);
                }
            } else if (gridBuffer == null) {
                startFullBuild(level, cx, cy, cz);
            }

            if (buildBuffer != null) {
                continueFullBuild(level);
            } else {
                shiftTowards(level, cx, cy, cz);
            }
        }

        if (applyDirtyUpdates(level)) {
            uploadBuffer(gridBuffer);
        }

        if (gridBuffer != null && Objects.equals(gridDimension, level.dimension())) {
            uniformGridPos.set(originX, originY, originZ);
        } else {
            uniformGridPos.set(cx - HALF, cy - HALF, cz - HALF);
        }
    }

    public static void markBlockDirty(BlockPos pos) {
        long packed = pos.asLong();
        synchronized (DIRTY_LOCK) {
            if (!DIRTY_SET.add(packed)) {
                return;
            }
            DIRTY_QUEUE.enqueue(packed);
            if (DIRTY_QUEUE.size() > MAX_DIRTY_BACKLOG) {
                rebuildRequested = true;
                clearDirty();
            }
        }
    }

    public static void clearLevel() {
        RenderSystem.assertOnRenderThreadOrInit();

        gridDimension = null;
        if (gridBuffer != null) {
            MemoryUtil.memFree(gridBuffer);
            gridBuffer = null;
        }

        buildDimension = null;
        buildIndex = 0;
        if (buildBuffer != null) {
            MemoryUtil.memFree(buildBuffer);
            buildBuffer = null;
        }

        clearDirty();
    }

    public static void close() {
        RenderSystem.assertOnRenderThreadOrInit();
        clearLevel();
        if (pboId != 0) {
            glDeleteBuffers(pboId);
            pboId = 0;
        }
        if (textureId != 0) {
            glDeleteTextures(textureId);
            textureId = 0;
        }
    }

    private static void clearDirty() {
        synchronized (DIRTY_LOCK) {
            DIRTY_QUEUE.clear();
            DIRTY_SET.clear();
        }
    }

    private static void startFullBuild(ClientLevel level, int cx, int cy, int cz) {
        buildDimension = level.dimension();
        buildOriginX = cx - HALF;
        buildOriginY = cy - HALF;
        buildOriginZ = cz - HALF;
        buildIndex = 0;
        if (buildBuffer == null) {
            buildBuffer = MemoryUtil.memAlloc(GRID_VOLUME);
        }
    }

    private static void continueFullBuild(ClientLevel level) {
        if (!Objects.equals(buildDimension, level.dimension())) {
            MemoryUtil.memFree(buildBuffer);
            buildBuffer = null;
            buildDimension = null;
            buildIndex = 0;
            return;
        }

        long deadline = System.nanoTime() + BUILD_BUDGET_NS;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        while (buildIndex < GRID_VOLUME && System.nanoTime() < deadline) {
            int lx = buildIndex & 63;
            int ly = (buildIndex >> 6) & 63;
            int lz = buildIndex >> 12;
            pos.set(buildOriginX + lx, buildOriginY + ly, buildOriginZ + lz);
            BlockState state = level.getBlockState(pos);
            buildBuffer.put(buildIndex, voxelOccupancy(level, pos, state));
            buildIndex++;
        }

        if (buildIndex < GRID_VOLUME) {
            return;
        }

        if (gridBuffer != null) {
            MemoryUtil.memFree(gridBuffer);
        }
        gridBuffer = buildBuffer;
        gridDimension = buildDimension;
        originX = buildOriginX;
        originY = buildOriginY;
        originZ = buildOriginZ;

        buildBuffer = null;
        buildDimension = null;
        buildIndex = 0;

        uploadBuffer(gridBuffer);
    }

    private static void shiftTowards(ClientLevel level, int cx, int cy, int cz) {
        if (gridBuffer == null || !Objects.equals(gridDimension, level.dimension())) {
            return;
        }

        int dx = cx - (originX + HALF);
        int dy = cy - (originY + HALF);
        int dz = cz - (originZ + HALF);

        if (Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) >= HALF) {
            startFullBuild(level, cx, cy, cz);
            return;
        }

        int steps = 0;
        boolean changed = false;
        while (steps < MAX_SLICE_UPDATES_PER_FRAME && (dx != 0 || dy != 0 || dz != 0)) {
            int ax = Math.abs(dx), ay = Math.abs(dy), az = Math.abs(dz);

            if (dx != 0 && ax >= ay && ax >= az) {
                if (dx > 0) {
                    shiftXPositive(level);
                    dx--;
                } else {
                    shiftXNegative(level);
                    dx++;
                }
            } else if (dz != 0 && az >= ay) {
                if (dz > 0) {
                    shiftZPositive(level);
                    dz--;
                } else {
                    shiftZNegative(level);
                    dz++;
                }
            } else if (dy != 0) {
                if (dy > 0) {
                    shiftYPositive(level);
                    dy--;
                } else {
                    shiftYNegative(level);
                    dy++;
                }
            } else {
                break;
            }

            changed = true;
            steps++;
        }

        if (changed) {
            uploadBuffer(gridBuffer);
        }
    }

    private static boolean applyDirtyUpdates(ClientLevel level) {
        if (gridBuffer == null && buildBuffer == null) {
            clearDirty();
            return false;
        }

        int toDrain;
        synchronized (DIRTY_LOCK) {
            toDrain = Math.min(DIRTY_QUEUE.size(), MAX_DIRTY_UPDATES_PER_FRAME);
            for (int i = 0; i < toDrain; i++) {
                DRAIN_SCRATCH[i] = DIRTY_QUEUE.dequeueLong();
                DIRTY_SET.remove(DRAIN_SCRATCH[i]);
            }
        }

        if (toDrain == 0) {
            return false;
        }

        boolean updatedGrid = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < toDrain; i++) {
            long packed = DRAIN_SCRATCH[i];
            int x = BlockPos.getX(packed);
            int y = BlockPos.getY(packed);
            int z = BlockPos.getZ(packed);
            pos.set(x, y, z);
            byte occupancy = voxelOccupancy(level, pos, level.getBlockState(pos));

            if (buildBuffer != null && Objects.equals(buildDimension, level.dimension())) {
                int bx = x - buildOriginX, by = y - buildOriginY, bz = z - buildOriginZ;
                if ((bx | by | bz) >= 0 && bx < GRID_SIZE && by < GRID_SIZE && bz < GRID_SIZE) {
                    buildBuffer.put(bx + by * GRID_SIZE + bz * SLICE_AREA, occupancy);
                }
            }

            if (gridBuffer != null && Objects.equals(gridDimension, level.dimension())) {
                int gx = x - originX, gy = y - originY, gz = z - originZ;
                if ((gx | gy | gz) >= 0 && gx < GRID_SIZE && gy < GRID_SIZE && gz < GRID_SIZE) {
                    gridBuffer.put(gx + gy * GRID_SIZE + gz * SLICE_AREA, occupancy);
                    updatedGrid = true;
                }
            }
        }

        return updatedGrid;
    }

    private static void shiftXPositive(ClientLevel level) {
        originX++;
        long base = MemoryUtil.memAddress(gridBuffer);
        for (int z = 0; z < GRID_SIZE; z++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                long row = base + (long) z * SLICE_AREA + (long) y * GRID_SIZE;
                MemoryUtil.memCopy(row + 1, row, GRID_SIZE - 1);
            }
        }
        fillSliceX(level, GRID_SIZE - 1, originX + GRID_SIZE - 1, base);
    }

    private static void shiftXNegative(ClientLevel level) {
        originX--;
        long base = MemoryUtil.memAddress(gridBuffer);
        for (int z = 0; z < GRID_SIZE; z++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                long row = base + (long) z * SLICE_AREA + (long) y * GRID_SIZE;
                MemoryUtil.memCopy(row, row + 1, GRID_SIZE - 1);
            }
        }
        fillSliceX(level, 0, originX, base);
    }

    private static void fillSliceX(ClientLevel level, int writeX, int worldX, long base) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = 0; z < GRID_SIZE; z++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                pos.set(worldX, originY + y, originZ + z);
                BlockState state = level.getBlockState(pos);
                MemoryUtil.memPutByte(base + (long) z * SLICE_AREA + (long) y * GRID_SIZE + writeX, voxelOccupancy(level, pos, state));
            }
        }
    }

    private static void shiftYPositive(ClientLevel level) {
        originY++;
        long base = MemoryUtil.memAddress(gridBuffer);
        for (int z = 0; z < GRID_SIZE; z++) {
            long plane = base + (long) z * SLICE_AREA;
            MemoryUtil.memCopy(plane + GRID_SIZE, plane, (long) GRID_SIZE * (GRID_SIZE - 1));
        }
        fillSliceY(level, GRID_SIZE - 1, originY + GRID_SIZE - 1, base);
    }

    private static void shiftYNegative(ClientLevel level) {
        originY--;
        long base = MemoryUtil.memAddress(gridBuffer);
        for (int z = 0; z < GRID_SIZE; z++) {
            long plane = base + (long) z * SLICE_AREA;
            MemoryUtil.memCopy(plane, plane + GRID_SIZE, (long) GRID_SIZE * (GRID_SIZE - 1));
        }
        fillSliceY(level, 0, originY, base);
    }

    private static void fillSliceY(ClientLevel level, int writeY, int worldY, long base) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = 0; z < GRID_SIZE; z++) {
            long row = base + (long) z * SLICE_AREA + (long) writeY * GRID_SIZE;
            for (int x = 0; x < GRID_SIZE; x++) {
                pos.set(originX + x, worldY, originZ + z);
                BlockState state = level.getBlockState(pos);
                MemoryUtil.memPutByte(row + x, voxelOccupancy(level, pos, state));
            }
        }
    }

    private static void shiftZPositive(ClientLevel level) {
        originZ++;
        long base = MemoryUtil.memAddress(gridBuffer);
        MemoryUtil.memCopy(base + SLICE_AREA, base, (long) SLICE_AREA * (GRID_SIZE - 1));
        fillSliceZ(level, GRID_SIZE - 1, originZ + GRID_SIZE - 1, base);
    }

    private static void shiftZNegative(ClientLevel level) {
        originZ--;
        long base = MemoryUtil.memAddress(gridBuffer);
        MemoryUtil.memCopy(base, base + SLICE_AREA, (long) SLICE_AREA * (GRID_SIZE - 1));
        fillSliceZ(level, 0, originZ, base);
    }

    private static void fillSliceZ(ClientLevel level, int writeZ, int worldZ, long base) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        long plane = base + (long) writeZ * SLICE_AREA;
        for (int y = 0; y < GRID_SIZE; y++) {
            long row = plane + (long) y * GRID_SIZE;
            for (int x = 0; x < GRID_SIZE; x++) {
                pos.set(originX + x, originY + y, worldZ);
                BlockState state = level.getBlockState(pos);
                MemoryUtil.memPutByte(row + x, voxelOccupancy(level, pos, state));
            }
        }
    }

    private static byte voxelOccupancy(ClientLevel level, BlockPos pos, BlockState state) {
        if (!state.canOcclude()) return 0;
        if (!state.getFluidState().isEmpty()) return 0;
        return state.isSolidRender(level, pos) ? (byte) 0xFF : 0;
    }

    private static void uploadBuffer(ByteBuffer buffer) {
        if (buffer == null || pboId == 0) {
            return;
        }

        buffer.position(0);
        buffer.limit(GRID_VOLUME);

        int unpackAlignment = glGetInteger(GL_UNPACK_ALIGNMENT);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, pboId);
        glBufferData(GL_PIXEL_UNPACK_BUFFER, buffer, GL_STREAM_DRAW);

        glBindTexture(GL_TEXTURE_3D, textureId);
        glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0, GRID_SIZE, GRID_SIZE, GRID_SIZE, GL_RED, GL_UNSIGNED_BYTE, 0L);
        glBindTexture(GL_TEXTURE_3D, 0);
        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
        glPixelStorei(GL_UNPACK_ALIGNMENT, unpackAlignment);
    }

    private static void ensureTexture() {
        if (textureId != 0) {
            return;
        }
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_3D, textureId);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        ByteBuffer zeros = MemoryUtil.memCalloc(GRID_VOLUME);
        glTexImage3D(GL_TEXTURE_3D, 0, GL_R8, GRID_SIZE, GRID_SIZE, GRID_SIZE, 0, GL_RED, GL_UNSIGNED_BYTE, zeros);
        MemoryUtil.memFree(zeros);
        glBindTexture(GL_TEXTURE_3D, 0);

        pboId = glGenBuffers();
        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, pboId);
        glBufferData(GL_PIXEL_UNPACK_BUFFER, GRID_VOLUME, GL_STREAM_DRAW);
        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
    }

    private static boolean hasOccludedLights() {
        Collection<LightTypeRenderer<?>> renderers = VeilRenderSystem.renderer().getLightRenderer().getRenderers().values();
        for (LightTypeRenderer<?> renderer : renderers) {
            if (renderer instanceof DDALightRenderer<?> ddaLightRenderer && ddaLightRenderer.hasOccludedLights()) {
                return true;
            }
        }
        return false;
    }

    public static Vector3fc getUniformGridPos() {
        return uniformGridPos;
    }

    public static int getTextureId() {
        return textureId;
    }
}
