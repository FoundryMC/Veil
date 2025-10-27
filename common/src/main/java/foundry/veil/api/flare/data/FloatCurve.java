package foundry.veil.api.flare.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.util.Easing;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Float curves are built from several easing functions strapped together.
 *
 * @author GuyApooye
 * @since 2.5.0
 */
public class FloatCurve {

    public static final Codec<FloatCurve> CODEC = KeyFrame.CODEC.listOf().xmap(FloatCurve::new, FloatCurve::getKeys);

    private final List<KeyFrame> keys;

    private FloatCurve(List<KeyFrame> keys) {
        this.keys = keys;
    }

    public FloatCurve() {
        this.keys = new ArrayList<>();
    }

    private List<KeyFrame> getKeys() {
        return this.keys;
    }

    public float evaluate(float time) {
        if (this.keys.isEmpty()) {
            return 0.0f;
        }
        KeyFrame first = this.keys.getFirst();
        if (time < first.time) {
            return first.value;
        }
        KeyFrame last = this.keys.getLast();
        if (time > last.time) {
            return last.value;
        }

        for (int i = 1; i < this.keys.size(); i++) {
            KeyFrame nextKey = this.keys.get(i);
            if (nextKey.time < time) {
                continue;
            }
            KeyFrame previousKey = this.keys.get(i - 1);
            float inBetweenTime = Mth.inverseLerp(time, previousKey.time, nextKey.time);
            return Mth.lerp(previousKey.easing.ease(inBetweenTime), previousKey.value, nextKey.value);
        }

        return time;
    }

    public static class Builder {
        private final ArrayList<KeyFrame> keys = new ArrayList<>();
        private final ArrayList<Float> times = new ArrayList<>();

        public Builder addKey(KeyFrame key) {
            if (this.times.contains(key.time)) {
                throw new IllegalArgumentException("Float curve cannot contain multiple keys with the same time!");
            }
            this.keys.add(key);
            this.times.add(key.time);
            return this;
        }

        public FloatCurve build() {
            Collections.sort(this.keys);
            return new FloatCurve(this.keys);
        }
    }

    public record KeyFrame(float time, float value, Easing easing) implements Comparable<KeyFrame> {
        public static final Codec<KeyFrame> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("time").forGetter(KeyFrame::time),
                Codec.FLOAT.fieldOf("value").forGetter(KeyFrame::value),
                Easing.CODEC.fieldOf("easing").forGetter(KeyFrame::easing)
        ).apply(instance, KeyFrame::new));

        @Override
        public int compareTo(@NotNull FloatCurve.KeyFrame other) {
            return Mth.sign(this.time - other.time);
        }
    }
}
