package foundry.veil.quasar.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import java.util.List;

public class ColorGradient {
    public static final Codec<ColorGradient> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RGBPoint.CODEC.listOf().fieldOf("rgb_points").forGetter(ColorGradient::getPoints),
                    AlphaPoint.CODEC.listOf().fieldOf("alpha_points").forGetter(ColorGradient::getAlphaPoints)
            ).apply(instance, ColorGradient::new)
    );
    private RGBPoint[] points;
    private AlphaPoint[] alphaPoints;

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

    public Vector4f getColor(float percentage) {
        return MathUtil.vec4fFromVec3(getRGB(percentage), getAlpha(percentage));
    }

    private float getAlpha(float percentage) {
        // if there are no alpha points, return the default alpha
        if (alphaPoints.length == 0) {
            return 1;
        }
        // if there is only one alpha point, return that alpha
        if (alphaPoints.length == 1) {
            return alphaPoints[0].alpha;
        }
        // loop over the alpha points to find the two points that the percentage is between
        for (int i = 0; i < alphaPoints.length - 1; i++) {
            if (percentage >= alphaPoints[i].percent && percentage <= alphaPoints[i + 1].percent) {
                // if the percentage is between two points, interpolate between them
                return Mth.lerp((percentage - alphaPoints[i].percent) / (alphaPoints[i + 1].percent - alphaPoints[i].percent), alphaPoints[i].alpha, alphaPoints[i + 1].alpha);
            }
        }
        // if the percentage is outside of the range of the alpha points, return the default alpha
        return 1;
    }

    private Vec3 getRGB(float percentage){
        // if there is only one point, return that color
        if (points.length == 1) {
            return points[0].color;
        }
        // loop over the points to find the two points that the percentage is between
        for (int i = 0; i < points.length - 1; i++) {
            if (percentage >= points[i].percent && percentage <= points[i + 1].percent) {
                // if the percentage is between two points, interpolate between them
                return points[i].color.lerp(points[i + 1].color, (percentage - points[i].percent) / (points[i + 1].percent - points[i].percent));
            }
        }
        // if the percentage is outside of the range of the points, return the default color
        return points[0].color;
    }

    public List<RGBPoint> getPoints() {
        return List.of(points);
    }

    public List<AlphaPoint> getAlphaPoints() {
        return List.of(alphaPoints);
    }

    static class RGBPoint {
        public static final Codec<RGBPoint> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.FLOAT.fieldOf("percent").forGetter(RGBPoint::getPercent),
                        Vec3.CODEC.fieldOf("color").forGetter(RGBPoint::getColor)
                ).apply(instance, RGBPoint::new)
        );
        float percent;
        Vec3 color;

        RGBPoint(float percent, Vec3 color) {
            this.percent = percent;
            this.color = color;
        }

        float getPercent() {
            return percent;
        }

        Vec3 getColor() {
            return color;
        }
    }

    static class AlphaPoint {
        public static final Codec<AlphaPoint> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.FLOAT.fieldOf("percent").forGetter(AlphaPoint::getPercent),
                        Codec.FLOAT.fieldOf("alpha").forGetter(AlphaPoint::getAlpha)
                ).apply(instance, AlphaPoint::new)
        );
        float percent;
        float alpha;

        AlphaPoint(float percent, float alpha) {
            this.percent = percent;
            this.alpha = alpha;
        }

        float getPercent() {
            return percent;
        }

        float getAlpha() {
            return alpha;
        }
    }
}
