package foundry.veil.api.flare.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.util.Easing;
import it.unimi.dsi.fastutil.floats.FloatOpenHashSet;
import it.unimi.dsi.fastutil.floats.FloatSet;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Float curves are built from several easing functions strapped together.
 *
 * @author GuyApooye
 * @since 2.5.0
 */
public class FloatCurve {

    public static final FloatCurve ZERO = new FloatCurve(new KeyFrame[0]);

    public static final Codec<FloatCurve> CODEC = KeyFrame.CODEC.listOf().
            xmap(list -> new FloatCurve(list.toArray(KeyFrame[]::new)),
                    curve -> Arrays.asList(curve.keys));

    private final KeyFrame[] keys;

    private FloatCurve(KeyFrame[] keys) {
        this.keys = keys;
    }

    public float evaluate(float time) {
        if (this.keys.length == 0) {
            return 0.0f;
        }

        KeyFrame first = this.keys[0];
        if (time < first.time) {
            return first.value;
        }
        KeyFrame last = this.keys[this.keys.length - 1];
        if (time > last.time) {
            return last.value;
        }

        for (int i = 1; i < this.keys.length; i++) {
            KeyFrame nextKey = this.keys[i];
            if (nextKey.time < time) {
                continue;
            }

            KeyFrame previousKey = this.keys[i - 1];
            float inBetweenTime = Mth.inverseLerp(time, previousKey.time, nextKey.time);
            return Mth.lerp(previousKey.easing.ease(inBetweenTime), previousKey.value, nextKey.value);
        }

        return time;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<KeyFrame> keys;
        private final FloatSet times;

        public Builder() {
            this.keys = new ArrayList<>();
            this.times = new FloatOpenHashSet();
        }

        public Builder addKey(KeyFrame key) {
            if (!this.times.add(key.time)) {
                throw new IllegalArgumentException("Float curve cannot contain multiple keys with the same time!");
            }
            this.keys.add(key);
            return this;
        }

        public FloatCurve build() {
            return new FloatCurve(this.keys.stream().sorted().toArray(KeyFrame[]::new));
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
            return Float.compare(this.time, other.time);
        }
    }
}
