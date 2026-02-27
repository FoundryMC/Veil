package foundry.veil.impl.client.render.light;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;

public final class VoxelShadowGrid {

    public static final int GRID_SIZE = 64;
    private static final int HALF = GRID_SIZE / 2;
    private static final double REBUILD_THRESHOLD_SQ = 12.0 * 12.0;

    private static final ResourceLocation POINT_SHADER = Veil.veilPath("light/point");
    private static final ResourceLocation AREA_SHADER = Veil.veilPath("light/area");

    private static int textureId;
    private static Vec3 gridCenter;

    private static volatile int generation;
    private static volatile Vec3 pendingCenter;
    private static volatile ResourceKey<Level> pendingDimension;
    private static volatile ByteBuffer pendingBuffer;
    private static volatile boolean rebuilding;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "veil-voxel-shadow-grid");
        t.setDaemon(true);
        return t;
    });

    private VoxelShadowGrid() {
    }

    public static void beforeRenderLights() {
        RenderSystem.assertOnRenderThread();

        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) {
            return;
        }

        ensureTexture();
        uploadPending(level);
        queueRebuildIfNeeded(level, client.gameRenderer.getMainCamera().getPosition());
        pushUniforms();
    }

    public static void clearLevel() {
        RenderSystem.assertOnRenderThreadOrInit();

        generation++;
        gridCenter = null;
        pendingCenter = null;
        pendingDimension = null;
        rebuilding = false;

        ByteBuffer buffer = pendingBuffer;
        pendingBuffer = null;
        if (buffer != null) {
            MemoryUtil.memFree(buffer);
        }
    }

    public static void close() {
        RenderSystem.assertOnRenderThreadOrInit();

        clearLevel();

        if (textureId != 0) {
            glDeleteTextures(textureId);
            textureId = 0;
        }

        EXECUTOR.shutdownNow();
    }

    private static void queueRebuildIfNeeded(ClientLevel level, Vec3 cameraPos) {
        Vec3 center = gridCenter;
        if (center != null && cameraPos.distanceToSqr(center) <= REBUILD_THRESHOLD_SQ) {
            return;
        }

        if (rebuilding) {
            return;
        }

        rebuilding = true;
        pendingCenter = cameraPos;
        pendingDimension = level.dimension();
        int capturedGeneration = generation;

        int cx = (int) Math.floor(cameraPos.x);
        int cy = (int) Math.floor(cameraPos.y);
        int cz = (int) Math.floor(cameraPos.z);

        ClientLevel capturedLevel = level;
        ResourceKey<Level> capturedDimension = level.dimension();
        EXECUTOR.submit(() -> {
            try {
                ByteBuffer buffer = buildBuffer(capturedLevel, cx, cy, cz);
                if (generation != capturedGeneration) {
                    MemoryUtil.memFree(buffer);
                    return;
                }
                pendingBuffer = buffer;
                pendingDimension = capturedDimension;
            } catch (Throwable t) {
                rebuilding = false;
            }
        });
    }

    private static void uploadPending(ClientLevel level) {
        ByteBuffer buffer = pendingBuffer;
        if (buffer == null) {
            return;
        }

        pendingBuffer = null;
        rebuilding = false;

        if (pendingDimension != null && pendingDimension != level.dimension()) {
            MemoryUtil.memFree(buffer);
            pendingCenter = null;
            pendingDimension = null;
            return;
        }

        uploadBuffer(buffer);
        MemoryUtil.memFree(buffer);
        gridCenter = pendingCenter;
        pendingCenter = null;
        pendingDimension = null;
    }

    private static ByteBuffer buildBuffer(ClientLevel level, int cx, int cy, int cz) {
        int total = GRID_SIZE * GRID_SIZE * GRID_SIZE;
        ByteBuffer buffer = MemoryUtil.memAlloc(total);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = 0; z < GRID_SIZE; z++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                for (int x = 0; x < GRID_SIZE; x++) {
                    pos.set(cx - HALF + x, cy - HALF + y, cz - HALF + z);
                    BlockState state = level.getBlockState(pos);
                    buffer.put(x + y * GRID_SIZE + z * GRID_SIZE * GRID_SIZE, voxelOccupancy(level, pos, state));
                }
            }
        }

        buffer.rewind();
        return buffer;
    }

    private static byte voxelOccupancy(ClientLevel level, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return 0;
        }

        if (!state.getFluidState().isEmpty()) {
            return 0;
        }

        Block block = state.getBlock();
        if (block instanceof SlabBlock) return 0;
        if (block instanceof StairBlock) return 0;
        if (block instanceof WallBlock) return 0;
        if (block instanceof FenceBlock) return 0;
        if (block instanceof FenceGateBlock) return 0;
        if (block instanceof CarpetBlock) return 0;
        if (block instanceof IronBarsBlock) return 0;
        if (block instanceof DoorBlock) return 0;
        if (block instanceof TrapDoorBlock) return 0;
        if (block instanceof LeavesBlock) return 0;
        if (block instanceof LiquidBlock) return 0;

        if (!state.canOcclude()) {
            return 0;
        }

        return state.isSolidRender(level, pos) ? (byte) 0xFF : 0;
    }

    private static void uploadBuffer(ByteBuffer buffer) {
        glBindTexture(GL_TEXTURE_3D, textureId);
        glTexImage3D(GL_TEXTURE_3D, 0, GL_R8, GRID_SIZE, GRID_SIZE, GRID_SIZE, 0, GL_RED, GL_UNSIGNED_BYTE, buffer);
        glBindTexture(GL_TEXTURE_3D, 0);
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

        int total = GRID_SIZE * GRID_SIZE * GRID_SIZE;
        ByteBuffer zeros = MemoryUtil.memCalloc(total);
        glTexImage3D(GL_TEXTURE_3D, 0, GL_R8, GRID_SIZE, GRID_SIZE, GRID_SIZE, 0, GL_RED, GL_UNSIGNED_BYTE, zeros);
        MemoryUtil.memFree(zeros);

        glBindTexture(GL_TEXTURE_3D, 0);
    }

    private static void pushUniforms() {
        Vec3 center = gridCenter;
        if (center == null) {
            center = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        }

        float originX = (float) (Math.floor(center.x) - HALF);
        float originY = (float) (Math.floor(center.y) - HALF);
        float originZ = (float) (Math.floor(center.z) - HALF);

        pushUniforms(POINT_SHADER, originX, originY, originZ);
        pushUniforms(AREA_SHADER, originX, originY, originZ);
    }

    private static void pushUniforms(ResourceLocation shader, float originX, float originY, float originZ) {
        ShaderProgram program = VeilRenderSystem.renderer().getShaderManager().getShader(shader);
        if (program == null || !program.isValid()) {
            return;
        }

        program.setSampler("BlockGrid", textureId);
        program.getUniformSafe("GridOrigin").setVector(originX, originY, originZ);
    }
}
