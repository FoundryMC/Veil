package foundry.veil.quasar.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.List;

public class ColorGradient {

    private static final Codec<Vector4fc> SINGLE_COLOR_CODEC = CodecUtil.VECTOR4F_CODEC.fieldOf("color").codec();
    private static final Codec<ColorGradient> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RGBPoint.CODEC.listOf().fieldOf("rgb_points").forGetter(ColorGradient::getPoints),
            AlphaPoint.CODEC.listOf().fieldOf("alpha_points").forGetter(ColorGradient::getAlphaPoints)
    ).apply(instance, ColorGradient::new));

    public static final Codec<ColorGradient> CODEC = Codec.either(SINGLE_COLOR_CODEC, FULL_CODEC)
            .xmap(either -> either.map(left -> new ColorGradient(left.x(), left.y(), left.z(), left.w()), right -> right),
                    gradient -> {
                        if (gradient.isConstant()) {
                            Vec3 point = gradient.points[0].color;
                            return Either.left(new Vector4f((float) point.x, (float) point.y, (float) point.z, gradient.alphaPoints[0].alpha));
                        }
                        return Either.right(gradient);
                    });

    private final RGBPoint[] points;
    private final AlphaPoint[] alphaPoints;

    public ColorGradient(float red, float green, float blue, float alpha) {
        this.points = new RGBPoint[]{new RGBPoint(0.0F, new Vec3(red, green, blue))};
        this.alphaPoints = new AlphaPoint[]{new AlphaPoint(0.0F, alpha)};
    }

    public ColorGradient(RGBPoint[] points, AlphaPoint[] alphaPoints) {
        this.points = points;
        this.alphaPoints = alphaPoints;
    }

    public ColorGradient(RGBPoint[] points) {
        this(points, new AlphaPoint[]{new AlphaPoint(0, 0), new AlphaPoint(1, 1)});
    }

    public ColorGradient(List<RGBPoint> points, List<AlphaPoint> alphaPoints) {
        this(points.toArray(new RGBPoint[0]), alphaPoints.toArray(new AlphaPoint[0]));
    }

    public ColorGradient(Vec3 startColor, Vec3 endColor, float startAlpha, float endAlpha) {
        this(new RGBPoint[]{new RGBPoint(0, startColor), new RGBPoint(1, endColor)}, new AlphaPoint[]{new AlphaPoint(0, startAlpha), new AlphaPoint(1, endAlpha)});
    }

    public ColorGradient(Vec3 startColor, Vec3 endColor) {
        this(new RGBPoint[]{new RGBPoint(0, startColor), new RGBPoint(1, endColor)});
    }

    public boolean isConstant() {
        return this.points.length == 1 && this.alphaPoints.length == 1;
    }

    public Vector4f getColor(float percentage) {
        return MathUtil.vec4fFromVec3(this.getRGB(percentage), this.getAlpha(percentage));
    }

    private float getAlpha(float percentage) {
        // if there are no alpha points, return the default alpha
        if (this.alphaPoints.length == 0) {
            return 1;
        }
        // if there is only one alpha point, return that alpha
        if (this.alphaPoints.length == 1) {
            return this.alphaPoints[0].alpha;
        }
        // loop over the alpha points to find the two points that the percentage is between
        for (int i = 0; i < this.alphaPoints.length - 1; i++) {
            if (percentage >= this.alphaPoints[i].percent && percentage <= this.alphaPoints[i + 1].percent) {
                // if the percentage is between two points, interpolate between them
                return Mth.lerp((percentage - this.alphaPoints[i].percent) / (this.alphaPoints[i + 1].percent - this.alphaPoints[i].percent), this.alphaPoints[i].alpha, this.alphaPoints[i + 1].alpha);
            }
        }
        // if the percentage is outside of the range of the alpha points, return the default alpha
        return 1;
    }

    private Vec3 getRGB(float percentage) {
        // if there is only one point, return that color
        if (this.points.length == 1) {
            return this.points[0].color;
        }
        // loop over the points to find the two points that the percentage is between
        for (int i = 0; i < this.points.length - 1; i++) {
            if (percentage >= this.points[i].percent && percentage <= this.points[i + 1].percent) {
                // if the percentage is between two points, interpolate between them
                return this.points[i].color.lerp(this.points[i + 1].color, (percentage - this.points[i].percent) / (this.points[i + 1].percent - this.points[i].percent));
            }
        }
        // if the percentage is outside of the range of the points, return the default color
        return this.points[0].color;
    }

    public List<RGBPoint> getPoints() {
        return List.of(this.points);
    }

    public List<AlphaPoint> getAlphaPoints() {
        return List.of(this.alphaPoints);
    }

    public static class RGBPoint {
        public static final Codec<RGBPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("percent").forGetter(RGBPoint::getPercent),
                Vec3.CODEC.fieldOf("color").forGetter(RGBPoint::getColor)
        ).apply(instance, RGBPoint::new));

        float percent;
        Vec3 color;

        RGBPoint(float percent, Vec3 color) {
            this.percent = percent;
            this.color = color;
        }

        float getPercent() {
            return this.percent;
        }

        Vec3 getColor() {
            return this.color;
        }
    }

    public static class AlphaPoint {
        public static final Codec<AlphaPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("percent").forGetter(AlphaPoint::getPercent),
                Codec.FLOAT.fieldOf("alpha").forGetter(AlphaPoint::getAlpha)
        ).apply(instance, AlphaPoint::new));
        float percent;
        float alpha;

        AlphaPoint(float percent, float alpha) {
            this.percent = percent;
            this.alpha = alpha;
        }

        float getPercent() {
            return this.percent;
        }

        float getAlpha() {
            return this.alpha;
        }
    }
}
