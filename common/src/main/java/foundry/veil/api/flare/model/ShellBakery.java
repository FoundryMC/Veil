package foundry.veil.api.flare.model;

import foundry.veil.Veil;
import foundry.veil.api.flare.data.model.*;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Bakes {@link UnbakedShell} into {@link BakedShell}.
 *
 * @since 2.5.0
 */
public final class ShellBakery {

    public static final ResourceLocation MISSING_SHELL_LOCATION = Veil.veilPath("builtin/missing");
    public static final BakedShell MISSING_SHELL;

    static {
        Map<Direction, ShellElementFace> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new ShellElementFace(new ShellFaceUV(0, 0, 16, 16, 0)));
        }
        UnbakedShell unbaked = new FlareShell(List.of(new ShellElement(
                new Vector3f(0, 0, 0),
                new Vector3f(16, 16, 16),
                null,
                faces
        )));

        MISSING_SHELL = Objects.requireNonNull(unbaked.bake(), "Missing Shell");
    }

    private ShellBakery() {
    }

    public static FlareBakedQuad bakeQuad(ShellElement element, ShellElementFace face, Direction direction) {
        return bakeQuad(element.from(), element.to(), face, direction, element.rotation());
    }

    public static FlareBakedQuad bakeQuad(
            Vector3fc from,
            Vector3fc to,
            ShellElementFace face,
            Direction facing,
            ShellElementRotation rotation) {
        Vector3f normal = facing.step();
        float[] vertexData = makeVertices(face.uv(), normal, facing, setupShape(from, to), rotation);
        return new FlareBakedQuad(vertexData);
    }

    private static float[] makeVertices(ShellFaceUV uvs, Vector3f normal, Direction direction, float[] posDiv16, @Nullable ShellElementRotation rotation) {
        float[] vertexData = new float[32];
        Matrix4f rotationMatrix = null;
        if (rotation != null) {
            float angle = rotation.angle() * Mth.DEG_TO_RAD;
            rotationMatrix = switch (rotation.axis()) {
                case X -> new Matrix4f().rotationX(angle);
                case Y -> new Matrix4f().rotationY(angle);
                case Z -> new Matrix4f().rotationZ(angle);
            };
            rotationMatrix.transformDirection(normal);
        }
        for (int i = 0; i < 4; i++) {
            bakeVertex(vertexData, i, normal, direction, uvs, posDiv16, rotation, rotationMatrix);
        }

        return vertexData;
    }

    private static void bakeVertex(float[] vertexData, int vertexIndex, Vector3f normal, Direction direction, ShellFaceUV shellFaceUV, float[] posDiv16, @Nullable ShellElementRotation rotation, @Nullable Matrix4f transform) {
        FaceInfo.VertexInfo vertexInfo = FaceInfo.fromFacing(direction).getVertexInfo(vertexIndex);
        Vector3f pos = new Vector3f(posDiv16[vertexInfo.xFace], posDiv16[vertexInfo.yFace], posDiv16[vertexInfo.zFace]);
        if (rotation != null && transform != null) {
            Vector3f origin = new Vector3f(rotation.origin()).div(16.0F).sub(0.5f, 0.0f, 0.5f);

            Vector3f offset = transform.transformPosition(new Vector3f(pos.x() - origin.x(), pos.y() - origin.y(), pos.z() - origin.z()));
            pos.set(offset.x() + origin.x(), offset.y() + origin.y(), offset.z() + origin.z());
        }
        fillVertex(vertexData, vertexIndex, pos, normal, shellFaceUV);
    }

    private static float[] setupShape(Vector3fc min, Vector3fc max) {
        float[] vertexPosition = new float[Direction.values().length];
        //center the center
        vertexPosition[FaceInfo.Constants.MIN_X] = min.x() / 16.0F - 0.5f;
        vertexPosition[FaceInfo.Constants.MIN_Y] = min.y() / 16.0F;
        vertexPosition[FaceInfo.Constants.MIN_Z] = min.z() / 16.0F - 0.5f;
        vertexPosition[FaceInfo.Constants.MAX_X] = max.x() / 16.0F - 0.5f;
        vertexPosition[FaceInfo.Constants.MAX_Y] = max.y() / 16.0F;
        vertexPosition[FaceInfo.Constants.MAX_Z] = max.z() / 16.0F - 0.5f;
        return vertexPosition;
    }

    private static void fillVertex(float[] vertexData, int vertexIndex, Vector3f pos, Vector3f normal, ShellFaceUV shellFaceUV) {
        int i = vertexIndex * 8;
        vertexData[i] = pos.x();
        vertexData[i + 1] = pos.y();
        vertexData[i + 2] = pos.z();
        vertexData[i + 3] = normal.x();
        vertexData[i + 4] = normal.y();
        vertexData[i + 5] = normal.z();
        vertexData[i + 6] = shellFaceUV.getU(vertexIndex) / 16.0F;
        vertexData[i + 7] = shellFaceUV.getV(vertexIndex) / 16.0F;
    }
}
