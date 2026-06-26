package foundry.veil.impl.quasar;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.color.Color;
import foundry.veil.api.client.color.Colorc;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import imgui.ImGui;
import imgui.flag.ImGuiDir;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ColorGradient implements EditorAttributeProvider {

    private static final Codec<Colorc> SINGLE_COLOR_CODEC = Color.ARGB_CODEC.fieldOf("color").codec();
    private static final Codec<ColorGradient> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RGBPoint.CODEC.listOf().fieldOf("rgb_points").forGetter(ColorGradient::getPoints),
            AlphaPoint.CODEC.listOf().fieldOf("alpha_points").forGetter(ColorGradient::getAlphaPoints)
    ).apply(instance, ColorGradient::new));

    public static final Codec<ColorGradient> CODEC = Codec.either(SINGLE_COLOR_CODEC, FULL_CODEC)
            .xmap(either -> either.map(ColorGradient::new, right -> right),
                    gradient -> {
                        if (gradient.isConstant()) {
                            Colorc point = gradient.points[0].color;
                            return Either.left(new Color(point.red(), point.green(), point.blue(), gradient.alphaPoints[0].alpha));
                        }
                        return Either.right(gradient);
                    });

    private RGBPoint[] points;
    private AlphaPoint[] alphaPoints;

    public ColorGradient(Colorc color) {
        this(color.red(), color.green(), color.blue(), color.alpha());
    }

    public ColorGradient(float red, float green, float blue, float alpha) {
        this.points = new RGBPoint[]{new RGBPoint(0.0F, new Color(red, green, blue))};
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

    public ColorGradient(Color startColor, Color endColor, float startAlpha, float endAlpha) {
        this(new RGBPoint[]{new RGBPoint(0, startColor), new RGBPoint(1, endColor)}, new AlphaPoint[]{new AlphaPoint(0, startAlpha), new AlphaPoint(1, endAlpha)});
    }

    public ColorGradient(Color startColor, Color endColor) {
        this(new RGBPoint[]{new RGBPoint(0, startColor), new RGBPoint(1, endColor)});
    }

    public boolean isConstant() {
        return this.points.length == 1 && this.alphaPoints.length == 1;
    }

    public Color getColor(float percentage) {
        return this.getColor(percentage, new Color());
    }

    public Color getColor(float percentage, Color store) {
        this.getRGB(percentage, store);
        store.alpha(this.getAlpha(percentage));
        return store;
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
        return this.alphaPoints[this.alphaPoints.length - 1].alpha;
    }

    private void getRGB(float percentage, Color store) {
        // if there is only one point, return that color
        if (this.points.length == 1) {
            store.set(this.points[0].color);
            return;
        }
        // loop over the points to find the two points that the percentage is between
        for (int i = 0; i < this.points.length - 1; i++) {
            if (percentage >= this.points[i].percent && percentage <= this.points[i + 1].percent) {
                // if the percentage is between two points, interpolate between them
                store.set( this.points[i].color.lerp(this.points[i + 1].color, (percentage - this.points[i].percent) / (this.points[i + 1].percent - this.points[i].percent), store));
           return;
            }
        }
        // if the percentage is outside of the range of the points, return the default color
        store.set(this.points[this.points.length - 1].color);
           return;
    }

    public List<RGBPoint> getPoints() {
        return List.of(this.points);
    }

    public List<AlphaPoint> getAlphaPoints() {
        return List.of(this.alphaPoints);
    }

    @Override
    public void renderImGuiAttributes() {
        ImGui.text("RGB Points");
        ImGui.indent();

        List<RGBPoint> pointsView = new ArrayList<>(List.of(points));
        boolean dirty = false;

        for (int i = 0; i < pointsView.size(); i++) {
            ImGui.pushID(i);
            ColorGradient.RGBPoint point = pointsView.get(i);
            ColorGradient.RGBPoint newPoint = renderRGBPoint(point);
            if (i != 0 && ImGui.arrowButton("##up", ImGuiDir.Up)) {
                ColorGradient.RGBPoint pointAbove = pointsView.get(i - 1);
                pointsView.set(i - 1, point);
                pointsView.set(i, pointAbove);
                dirty = true;
            }
            if (i != 0 && i != pointsView.size() - 1) ImGui.sameLine();
            if (i != pointsView.size() - 1 && ImGui.arrowButton("##down", ImGuiDir.Down)) {
                ColorGradient.RGBPoint pointBelow = pointsView.get(i + 1);
                pointsView.set(i + 1, point);
                pointsView.set(i, pointBelow);
                dirty = true;
            }

            if (newPoint != null) {
                if (newPoint.percent() == Float.MIN_VALUE) {
                    pointsView.remove(i--);
                } else {
                    pointsView.set(i, newPoint);
                }
                dirty = true;
            }
            ImGui.separator();
            ImGui.popID();
        }

        if (ImGui.button("New RGB Point")) {
            if (pointsView.isEmpty()) {
                pointsView.add(new ColorGradient.RGBPoint(0.0f, Color.WHITE));
            } else {
                pointsView.add(new ColorGradient.RGBPoint(Math.min(pointsView.getLast().percent() + 0.1f, 1.0f), Color.WHITE));
            }
            dirty = true;
        }

        ImGui.unindent();

        ImGui.text("Alpha Points");
        ImGui.indent();

        List<AlphaPoint> alphaPointsView = new ArrayList<>(List.of(alphaPoints));

        for (int i = 0; i < alphaPointsView.size(); i++) {
            ImGui.pushID(i + 999);
            ColorGradient.AlphaPoint point = alphaPointsView.get(i);
            ColorGradient.AlphaPoint newPoint = renderAlphaPoint(point);
            if (newPoint != null) {
                if (newPoint.percent() == Float.MIN_VALUE) {
                    alphaPointsView.remove(i--);
                } else {
                    alphaPointsView.set(i, newPoint);
                }
                dirty = true;
            }
            ImGui.popID();
        }

        if (ImGui.button("New Alpha Point")) {
            if (alphaPointsView.isEmpty()) {
                alphaPointsView.add(new ColorGradient.AlphaPoint(0.0f, 1.0f));
            } else {
                alphaPointsView.add(new ColorGradient.AlphaPoint(Math.min(alphaPointsView.getLast().percent() + 0.1f, 1.0f), 1.0f));
            }
            dirty = true;
        }

        ImGui.unindent();

        if (dirty) {
            points = pointsView.toArray(new RGBPoint[0]);
            alphaPoints = alphaPointsView.toArray(new AlphaPoint[0]);
        }
    }

    @Nullable
    private ColorGradient.RGBPoint renderRGBPoint(ColorGradient.RGBPoint point) {
        float[] editPercent = new float[]{point.percent()};
        float[] editColor = new float[]{point.color().red(), point.color().green(), point.color().blue()};
        boolean percentEdited = ImGui.dragScalar("percent", editPercent, 0.005f, 0, 1);
        boolean colorEdited = ImGui.colorEdit3("rgb", editColor);

        ColorGradient.RGBPoint toReturn = null;

        if (percentEdited || colorEdited) {
            toReturn = new ColorGradient.RGBPoint(editPercent[0], new Color(editColor[0], editColor[1], editColor[2]));
        }
        if (ImGui.button("Remove Point")) {
            toReturn = new ColorGradient.RGBPoint(Float.MIN_VALUE, Color.BLACK);
        }

        return toReturn;
    }

    @Nullable
    private ColorGradient.AlphaPoint renderAlphaPoint(ColorGradient.AlphaPoint point) {
        float[] editPercent = new float[]{point.percent()};
        float[] editAlpha = new float[]{point.alpha()};
        boolean percentEdited = ImGui.dragScalar("percent", editPercent, 0.005f, 0, 1);
        boolean alphaEdited = ImGui.dragScalar("alpha", editAlpha, 0.005f, 0, 1);

        ColorGradient.AlphaPoint toReturn = null;

        if (percentEdited || alphaEdited) {
            toReturn = new ColorGradient.AlphaPoint(editPercent[0], editAlpha[0]);
        }
        if (ImGui.button("Remove Point")) {
            toReturn = new ColorGradient.AlphaPoint(Float.MIN_VALUE, 0);
        }

        return toReturn;
    }

    public record RGBPoint(float percent, Colorc color) {
        public static final Codec<RGBPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("percent").forGetter(RGBPoint::percent),
                Color.RGB_CODEC.fieldOf("color").forGetter(RGBPoint::color)
        ).apply(instance, RGBPoint::new));
    }

    public record AlphaPoint(float percent, float alpha) {
        public static final Codec<AlphaPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("percent").forGetter(AlphaPoint::percent),
                Codec.FLOAT.fieldOf("alpha").forGetter(AlphaPoint::alpha)
        ).apply(instance, AlphaPoint::new));
    }
}
