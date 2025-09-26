package foundry.veil.api.flare.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.util.EasingWrapper;
import net.minecraft.util.Mth;
import foundry.veil.api.util.CodecUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Float curves are built from several easing functions strapped together.
 *
 * @author GuyApooye
 */
public class FloatCurve {
    
    public static final Codec<FloatCurve> CODEC = Key.CODEC.listOf().xmap(FloatCurve::new, FloatCurve::getKeys);
    
    private final List<Key> keys;

    private FloatCurve(List<Key> keys) {
        this.keys = keys;
    }

    public FloatCurve() {
        this.keys = new ArrayList<>();
    }

    private List<Key> getKeys() {
        return keys;
    }

    public float evaluate(float time) {

        if (keys.isEmpty()) return 0.0f;
        Key first = keys.getFirst();
        if (time < first.time) return first.value;
        Key last = keys.getLast();
        if (time > last.time) return last.value;

        for (int i = 1; i < keys.size(); i++) {
            Key nextKey = keys.get(i);
            if (nextKey.time < time) continue;
            Key previousKey = keys.get(i-1);
            float inBetweenTime = Mth.inverseLerp(time, previousKey.time, nextKey.time);
            return Mth.lerp(previousKey.easing.ease(inBetweenTime), previousKey.value, nextKey.value);
        }

        return time;
    }

    public static class Builder {
        private final ArrayList<Key> keys = new ArrayList<>();
        private final ArrayList<Float> times = new ArrayList<>();
        public Builder addKey(Key key) {
            if (times.contains(key.time)) throw new IllegalArgumentException("Float curve cannot contain multiple keys with the same time!");
            keys.add(key);
            times.add(key.time);
            return this;
        }
        public FloatCurve build() {
            Collections.sort(keys);
            return new FloatCurve(keys);
        }
    }

    public record Key(float time, float value, EasingWrapper easing) implements Comparable<Key> {
        public static final Codec<Key> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("time").forGetter(Key::time),
                Codec.FLOAT.fieldOf("value").forGetter(Key::value),
                CodecUtil.EASING_CODEC.fieldOf("easing").forGetter(Key::easing)
        ).apply(instance, Key::new));
        
        @Override
        public int compareTo(@NotNull FloatCurve.Key other) {
            return Mth.sign(this.time - other.time);
        }
    }
}
