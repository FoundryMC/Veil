package foundry.veil.test;

import com.mojang.math.Vector3f;
import foundry.veil.color.Color;
import foundry.veil.postprocessing.DynamicEffectInstance;

import java.util.function.BiConsumer;

public class EnergySphereFx extends DynamicEffectInstance {
    public Vector3f center;
    public Color color;
    public float radius;
    public float intensity;

    public EnergySphereFx(Vector3f pos, Color color, float radius, float intensity) {
        this.center = pos;
        this.color = color;
        this.radius = radius;
        this.intensity = intensity;
    }

    public EnergySphereFx(Vector3f pos, float radius, float intensity) {
        this(pos, new Color(.2F, .1F, .4F, 1F), radius, intensity);
    }

    @Override
    public void writeDataToBuffer(BiConsumer<Integer, Float> writer) {
        writer.accept(0, center.x());
        writer.accept(1, center.y());
        writer.accept(2, center.z());
        writer.accept(3, color.getRed());
        writer.accept(4, color.getGreen());
        writer.accept(5, color.getBlue());
        writer.accept(6, radius);
        writer.accept(7, intensity);
    }
}
