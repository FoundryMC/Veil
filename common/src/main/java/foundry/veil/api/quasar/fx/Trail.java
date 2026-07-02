package foundry.veil.api.quasar.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.quasar.emitters.module.render.TrailSettings;
import foundry.veil.api.util.EnumCodec;
import foundry.veil.impl.quasar.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class Trail {

    public enum TilingMode {
        NONE,
        STRETCH,
        REPEAT;

        public static final Codec<TilingMode> CODEC = EnumCodec.<TilingMode>builder("Tiling Mode")
                .values(values())
                .build();
    }

    private List<Vec3> points = new ArrayList<>();
    private final List<Vec3> rotations = new ArrayList<>();
    private int color;
    private Function<Float, Float> widthFunction;
    private int length = 100;
    private boolean billboard = false;
    private TilingMode tilingMode = TilingMode.STRETCH;
    private int frequency = 1;
    private float minDistance = 0f;
    private ResourceLocation texture = null;
    private boolean additive = false;
    private boolean parentRotation = false;
    private int timeout = 0;

    public Trail(TrailSettings settings) {
        this(MathUtil.colorFromVec4f(settings.getTrailColor()), (ageScale) -> settings.getTrailWidthModifier().modify(ageScale, 1));
        this.billboard = settings.getBillboard();
        this.length = settings.getTrailLength();
        this.frequency = settings.getTrailFrequency();
        this.tilingMode = settings.getTilingMode();
        this.texture = settings.getTrailTexture();
        this.additive = settings.isAdditive();
        this.parentRotation = settings.getParentRotation();
    }

    public Trail(Collection<Vec3> points, int color, Function<Float, Float> widthFunction) {
        this.points.addAll(points);
        this.color = color;
        this.widthFunction = widthFunction;
    }

    public Trail(int color, Function<Float, Float> widthFunction) {
        this(List.of(), color, widthFunction);
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
        if (points.length > this.length) {
            Vec3[] newPoints = new Vec3[this.length];
            System.arraycopy(points, points.length - this.length, newPoints, 0, this.length);
            points = newPoints;
        }
        this.points = new ArrayList<>(Arrays.asList(points));
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

    public boolean isAdditive() {
        return this.additive;
    }

    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    public void setWidthFunction(Function<Float, Float> widthFunction) {
        this.widthFunction = widthFunction;
    }

    public @Nullable ResourceLocation getTexture() {
        return this.texture;
    }

    public int getLength() {
        return this.length;
    }

    public void pushRotatedPoint(Vec3 point, Vec3 rotation) {
        if (this.timeout > 80 && this.timeout % 5 == 0 && !this.points.isEmpty()) {
            this.points.removeLast();
            this.rotations.removeLast();
            return;
        }
        if (this.points.isEmpty()) {
            this.points.addFirst(point);
            this.rotations.addFirst(rotation);
            return;
        }
        if (this.points.getLast().distanceTo(point) < this.minDistance) {
            this.timeout++;
            return;
        }
        // test if point is same as last point
        if (!this.points.isEmpty() && this.points.getLast().equals(point)) {
            this.timeout++;
            return;
        }
        this.timeout = 0;
        this.points.addFirst(point);
        this.rotations.addFirst(rotation);

        if (this.points.size() > this.length) {
            this.points.removeLast();
            this.rotations.removeLast();
        }
    }

    @ApiStatus.Internal
    public void render(MatrixStack stack, VertexConsumer consumer, int light, float partialTicks, Vec3 target, Vec3 targetRotation) {
        // Not enough geometry to render anything
        if (this.points.isEmpty()) {
            return;
        }

        List<Vec3> lerpedPoints = new ArrayList<>();
        for (int i = 0; i < this.points.size(); i++) {
            Vec3 lerpedPoint = this.points.get(i);
            if (i == 0) {
                lerpedPoint = lerpedPoint.lerp(target, partialTicks);
            } else {
                lerpedPoint = lerpedPoint.lerp(this.points.get(i - 1), partialTicks);
            }
            lerpedPoints.add(lerpedPoint);
        }

        List<Vec3> lerpedRotations = new ArrayList<>();
        for (int i = 0; i < this.rotations.size(); i++) {
            Vec3 lerpedRotation = this.rotations.get(i);
            if (i == 0) {
                lerpedRotation = lerpedRotation.lerp(targetRotation, partialTicks);
            } else {
                lerpedRotation = lerpedRotation.lerp(this.rotations.get(i - 1), partialTicks);
            }
            lerpedRotations.add(lerpedRotation);
        }

        Vector3f[][] corners = new Vector3f[lerpedPoints.size()][2];
        for (int i = 0; i < lerpedPoints.size(); i++) {
            if (i % this.frequency != 0) {
                continue;
            }
            float width = this.widthFunction.apply((float) i / (lerpedPoints.size() - 1));
            Vector3f topOffset = new Vector3f(0, (width / 2f), 0);
            Vector3f bottomOffset = new Vector3f(0, -(width / 2f), 0);
            if (this.billboard) {
                Vec3 a = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(lerpedPoints.get(i)).normalize();
                Vector3f cameraDirection = new Vector3f((float) a.x, (float) a.y, (float) a.z);
                Vec3 b = lerpedPoints.get(Math.min(i + this.frequency, lerpedPoints.size() - 1)).subtract(lerpedPoints.get(i)).normalize();
                Vector3f dirToNextPoint = new Vector3f((float) b.x(), (float) b.y(), (float) b.z());
                Vector3f axis = new Vector3f(cameraDirection);
                // invert the axis
                axis.mul(-1);
                axis.cross(dirToNextPoint);
                topOffset = new Vector3f(axis);
                topOffset.mul(width / 2f);
                bottomOffset = new Vector3f(axis);
                bottomOffset.mul(-width / 2f);
            } else if (lerpedRotations.get(i) != null && this.parentRotation) {
                Vec3 a = lerpedRotations.get(Math.min(i + this.frequency, lerpedRotations.size() - 1));
                Vector3f cameraDirection = new Vector3f((float) a.x, (float) a.y, (float) a.z);
                Vec3 b = lerpedPoints.get(Math.min(i + this.frequency, lerpedPoints.size() - 1)).subtract(lerpedPoints.get(i)).normalize();
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
            topOffset.add((float) lerpedPoints.get(i).x, (float) lerpedPoints.get(i).y, (float) lerpedPoints.get(i).z);
            bottomOffset.add((float) lerpedPoints.get(i).x, (float) lerpedPoints.get(i).y, (float) lerpedPoints.get(i).z);
            corners[i / this.frequency][0] = topOffset;
            corners[i / this.frequency][1] = bottomOffset;
        }
        this.renderPoints(stack, consumer, light, corners, this.color);
    }

    private void renderPoints(MatrixStack stack, VertexConsumer consumer, int light, Vector3f[][] corners, int color) {
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        int a = color >> 24 & 255;
        for (int i = 0; i < corners.length; i++) {
            Vector3f top = corners[i][0];
            Vector3f bottom = corners[i][1];
            if (top == null || bottom == null) {
                continue;
            }
            float u = 0;
            if (this.tilingMode == TilingMode.STRETCH) {
                u = (float) i / (corners.length - 1);
            }
            Matrix4f matrix4f = stack.position();
            consumer.addVertex(matrix4f, bottom.x(), bottom.y(), bottom.z()).setColor(r, g, b, a).setUv(u, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(matrix4f, top.x(), top.y(), top.z()).setColor(r, g, b, a).setUv(u, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        }
    }
}
