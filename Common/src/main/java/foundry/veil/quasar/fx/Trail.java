package foundry.veil.quasar.fx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Locale;
import java.util.function.Function;

public class Trail {

    public enum TilingMode {
        NONE,
        STRETCH,
        REPEAT;

        public static final Codec<TilingMode> CODEC = Codec.STRING.flatXmap(name -> {
            for (TilingMode value : TilingMode.values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return DataResult.success(value);
                }
            }
            return DataResult.error(() -> "Unknown Tiling Mode");
        }, tilingMode1 -> DataResult.success(tilingMode1.name().toLowerCase(Locale.ROOT)));
    }

    private Vec3[] points;
    private Vec3[] rotations;
    private int color;
    private Function<Float, Float> widthFunction;
    private int length = 100;
    private boolean billboard = false;
    private TilingMode tilingMode = TilingMode.STRETCH;
    private int frequency = 1;
    private float minDistance = 0f;
    private ResourceLocation texture = null;
    private boolean parentRotation = false;
    private int timeout = 0;

    public Trail(Vec3[] points, int color, Function<Float, Float> widthFunction) {
        this.points = points;
        this.color = color;
        this.widthFunction = widthFunction;
    }

    public Trail(int color, Function<Float, Float> widthFunction) {
        this(new Vec3[]{Vec3.ZERO}, color, widthFunction);
    }

    public void setParentRotation(boolean parentRotation) {
        this.parentRotation = parentRotation;
    }

    public void setTilingMode(TilingMode tilingMode) {
        this.tilingMode = tilingMode;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setMinDistance(float minDistance) {
        this.minDistance = minDistance;
    }

    public void setPoints(Vec3[] points) {
        if (points.length > length) {
            Vec3[] newPoints = new Vec3[length];
            System.arraycopy(points, points.length - length, newPoints, 0, length);
            points = newPoints;
        }
        this.points = points;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setBillboard(boolean billboard) {
        this.billboard = billboard;
    }

    public void setWidthFunction(Function<Float, Float> widthFunction) {
        this.widthFunction = widthFunction;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public int getLength() {
        return length;
    }

    public void pushPoint(Vec3 point) {
        if (timeout > Minecraft.getInstance().getWindow().getRefreshRate() * 5 && timeout % 3 == 0) {
            //remove the last point in the array
            Vec3[] newPoints = new Vec3[points.length - 1];
            System.arraycopy(points, 1, newPoints, 0, points.length - 1);
            points = newPoints;
            return;
        }
        if (points.length == 0) {
            points = new Vec3[]{point};
            return;
        }
        if (points[points.length - 1].distanceTo(point) < minDistance) {
            timeout++;
            return;
        }
        // test if point is same as last point
        if (points[points.length - 1].equals(point)) {
            timeout++;
            return;
        }
        // add point to end of array and remove first point if array is longer than length
        if (points[0] == Vec3.ZERO) {
            points[0] = point;
            return;
        }
        timeout = 0;
        Vec3[] newPoints = new Vec3[points.length + 1];
        System.arraycopy(points, 0, newPoints, 0, points.length);
        newPoints[points.length] = point;
        if (newPoints.length > length) {
            Vec3[] newPoints2 = new Vec3[length];
            System.arraycopy(newPoints, 1, newPoints2, 0, length);
            newPoints = newPoints2;
        }
        points = newPoints;
    }

    public void pushRotatedPoint(Vec3 point, Vec3 rotation) {
        if (timeout > Minecraft.getInstance().getWindow().getRefreshRate() * 5 && timeout % 5 == 0 && points.length > 0) {
            //remove the last point in the array
            Vec3[] newPoints = new Vec3[points.length - 1];
            System.arraycopy(points, 1, newPoints, 0, points.length - 1);
            points = newPoints;
            Vec3[] newRotations = new Vec3[rotations.length - 1];
            System.arraycopy(rotations, 1, newRotations, 0, rotations.length - 1);
            rotations = newRotations;
            return;
        }
        if (points.length == 0) {
            points = new Vec3[]{point};
            rotations = new Vec3[]{rotation};
            return;
        }
        if (points[0] == Vec3.ZERO) {
            points[0] = point;
            rotations = new Vec3[]{rotation};
            return;
        }
        if (points[points.length - 1].distanceTo(point) < minDistance) {
            timeout++;
            return;
        }
        // test if point is same as last point
        if (points.length > 0 && points[points.length - 1].equals(point)) {
            timeout++;
            return;
        }
        if (rotations == null) {
            rotations = new Vec3[]{rotation};
        }
        // add point to end of array and remove first point if array is longer than length
        Vec3[] newPoints = new Vec3[points.length + 1];
        Vec3[] newRotations = new Vec3[points.length + 1];
        System.arraycopy(points, 0, newPoints, 0, points.length);
        System.arraycopy(rotations, 0, newRotations, 0, rotations.length);
        newPoints[points.length] = point;
        newRotations[rotations.length] = rotation;
        if (newPoints.length > length) {
            Vec3[] newPoints2 = new Vec3[length];
            Vec3[] newRotations2 = new Vec3[length];
            System.arraycopy(newPoints, 1, newPoints2, 0, length);
            System.arraycopy(newRotations, 1, newRotations2, 0, length);
            newPoints = newPoints2;
            newRotations = newRotations2;
        }
        points = newPoints;
        rotations = newRotations;
    }

    public void render(PoseStack stack, VertexConsumer consumer, int light) {
        stack.pushPose();
        RenderSystem.disableCull();
        Vector3f[][] corners = new Vector3f[points.length][2];
        for (int i = 0; i < points.length; i++) {
            if (i % frequency != 0) continue;
            float width = widthFunction.apply((float) i / (points.length - 1));
            Vector3f topOffset = new Vector3f(0, (width / 2f), 0);
            Vector3f bottomOffset = new Vector3f(0, -(width / 2f), 0);
            if (billboard) {
                Vec3 a = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(points[i]).normalize();
                Vector3f cameraDirection = new Vector3f((float) a.x, (float) a.y, (float) a.z);
                Vec3 b = points[Math.min(i + frequency, points.length - 1)].subtract(points[i]).normalize();
                Vector3f dirToNextPoint = new Vector3f((float) b.x(), (float) b.y(), (float) b.z());
                Vector3f axis = new Vector3f(cameraDirection);
                // invert the axis
                axis.mul(-1);
                axis.cross(dirToNextPoint);
                topOffset = new Vector3f(axis);
                topOffset.mul(width / 2f);
                bottomOffset = new Vector3f(axis);
                bottomOffset.mul(-width / 2f);
            } else if (rotations[i] != null && parentRotation) {
                Vec3 a = rotations[Math.min(i + frequency, rotations.length - 1)];
                Vector3f cameraDirection = new Vector3f((float) a.x, (float) a.y, (float) a.z);
                Vec3 b = points[Math.min(i + frequency, points.length - 1)].subtract(points[i]).normalize();
                Vector3f dirToNextPoint = new Vector3f((float) b.x(), (float) b.y(), (float) b.z());
                Vector3f axis = new Vector3f(cameraDirection);
                // invert the axis
                axis.mul(-1);
                axis.cross(dirToNextPoint);
                topOffset = new Vector3f(axis);
                topOffset.mul(width / 2f);
                bottomOffset = new Vector3f(axis);
                bottomOffset.mul(-width / 2f);
            }
            topOffset.add((float) points[i].x, (float) points[i].y, (float) points[i].z);
            bottomOffset.add((float) points[i].x, (float) points[i].y, (float) points[i].z);
            corners[i / frequency][0] = topOffset;
            corners[i / frequency][1] = bottomOffset;
        }
        renderPoints(stack, consumer, light, corners, color);
        RenderSystem.enableCull();
        stack.popPose();
    }

    private void renderPoints(PoseStack stack, VertexConsumer consumer, int light, Vector3f[][] corners, int color) {
        stack.pushPose();
        float r = (color >> 16 & 255) / 255f;
        float g = (color >> 8 & 255) / 255f;
        float b = (color & 255) / 255f;
        float a = (color >> 24 & 255) / 255f;
        for (int i = 0; i < corners.length - 1; i++) {
            Vector3f top = corners[i][0];
            Vector3f bottom = corners[i][1];
            Vector3f nextTop = corners[i + 1][0];
            Vector3f nextBottom = corners[i + 1][1];
            if (nextTop == null || nextBottom == null || top == null || bottom == null) continue;
            float u = 0;
            float u1 = 1;
            if (tilingMode == TilingMode.STRETCH) {
                u = (float) i / (corners.length - 1);
                u1 = (float) (i + 1) / (corners.length - 1);
            }
            consumer.vertex(stack.last().pose(), bottom.x(), bottom.y(), bottom.z()).color(r, g, b, a).uv(u, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
            consumer.vertex(stack.last().pose(), top.x(), top.y(), top.z()).color(r, g, b, a).uv(u, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
            consumer.vertex(stack.last().pose(), nextTop.x(), nextTop.y(), nextTop.z()).color(r, g, b, a).uv(u1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
            consumer.vertex(stack.last().pose(), nextBottom.x(), nextBottom.y(), nextBottom.z()).color(r, g, b, a).uv(u1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
        }
        stack.popPose();
    }
}
