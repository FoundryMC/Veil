package foundry.veil.api.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Codec for serializing/deserializing enum constants.
 *
 * @param <T> The enum type to encode/decode
 * @author Ocelot
 * @since 1.0.0
 */
public class EnumCodec<T extends Enum<?>> implements Codec<T> {

    private final String name;
    private final T[] values;
    private final String valid;
    private final Function<T, String> toString;
    private final boolean ordinal;
    private final StreamCodec<ByteBuf, T> streamCodec;

    private EnumCodec(String name, T[] values, Function<T, String> toString, boolean ordinal) {
        this.name = name;
        this.values = values;
        this.valid = Arrays.stream(values).map(toString).collect(Collectors.joining(", "));
        this.toString = toString;
        this.ordinal = ordinal;
        this.streamCodec = StreamCodec.of((buf, value) -> VarInt.write(buf, this.getIndex(value)), buf -> {
            int i = VarInt.read(buf);
            if (i < 0 || i >= this.values.length) {
                throw new DecoderException("Unknown " + this.name + " with index: " + i);
            }
            return this.values[i];
        });
    }

    private int getIndex(T value) {
        if (this.ordinal) {
            return value.ordinal();
        }
        for (int i = 0; i < this.values.length; i++) {
            if (this.values[i] == value) {
                return i;
            }
        }
        throw new EncoderException("Invalid " + this.name + ": " + this.toString.apply(value));
    }

    @Override
    public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
        if (ops.compressMaps()) {
            return ops.getNumberValue(input).flatMap(index -> {
                int i = index.intValue();
                if (i < 0 || i >= this.values.length) {
                    return DataResult.error(() -> "Unknown " + this.name + " with index: " + i);
                }
                return DataResult.success(Pair.of(this.values[i], input));
            });
        } else {
            return ops.getStringValue(input).flatMap(valueName -> {
                for (T value : this.values) {
                    if (this.toString.apply(value).equalsIgnoreCase(valueName)) {
                        return DataResult.success(Pair.of(value, input));
                    }
                }
                return DataResult.error(() -> "Unknown " + this.name + ": " + valueName + ". Valid options: " + this.valid);
            });
        }
    }

    @Override
    public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix) {
        if (ops.compressMaps()) {
            return DataResult.success(ops.createInt(this.getIndex(input)));
        } else {
            return DataResult.success(ops.createString(this.toString.apply(input)));
        }
    }

    /**
     * @return A stream codec for packets
     */
    public StreamCodec<ByteBuf, T> streamCodec() {
        return this.streamCodec;
    }

    public static <T extends Enum<?>> Builder<T> builder(String name) {
        return new Builder<>(name);
    }

    /**
     * Builder for creating a new {@link EnumCodec}.
     *
     * @param <T> The enum type to encode/decode
     * @author Ocelot
     */
    @SuppressWarnings("unchecked")
    public static class Builder<T extends Enum<?>> {

        private static final Function<Enum<?>, String> LOWERCASE = e -> e.name().toLowerCase(Locale.ROOT);
        private static final Function<Enum<?>, String> UPPERCASE = e -> e.name().toUpperCase(Locale.ROOT);

        private final String name;
        private T[] values;
        private Function<T, String> toString;

        private Builder(String name) {
            this.name = name;
            this.values = null;
            this.toString = (Function<T, String>) LOWERCASE;
        }

        /**
         * Sets the valid values of this codec to all the enum constants provided by the specified class.
         *
         * @param clazz The class to get the constants of
         */
        public Builder<T> values(Class<T> clazz) {
            this.values = clazz.getEnumConstants();
            return this;
        }

        /**
         * Sets the valid values of this codec to the specified values.
         *
         * @param values The values to use
         */
        @SafeVarargs
        public final Builder<T> values(T... values) {
            this.values = values;
            return this;
        }

        /**
         * Specifies what function to use when getting the name of an enum constant.
         *
         * @param toString The new function
         */
        public Builder<T> toStringFunction(Function<T, String> toString) {
            this.toString = Objects.requireNonNull(toString, "toString");
            return this;
        }

        /**
         * Specifies the standard names to be uppercase.
         */
        public Builder<T> uppercase() {
            this.toString = (Function<T, String>) UPPERCASE;
            return this;
        }

        /**
         * Specifies the standard names to be lowercase.
         */
        public Builder<T> lowercase() {
            this.toString = (Function<T, String>) LOWERCASE;
            return this;
        }

        /**
         * @return A new enum codec
         */
        public EnumCodec<T> build() {
            Objects.requireNonNull(this.values, "Values must be specified");
            if (this.values.length == 0) {
                throw new IllegalArgumentException("At least 1 value must be specified");
            }

            if (Arrays.stream(this.values).distinct().count() != this.values.length) {
                throw new IllegalArgumentException("All values must be unique");
            }

            Class<?> clazz = this.values[0].getClass();
            while (!clazz.isEnum()) {
                clazz = clazz.getSuperclass();
            }
            Enum<?>[] enumValues = (Enum<?>[]) clazz.getEnumConstants();
            return new EnumCodec<>(this.name, this.values, this.toString, enumValues.length == this.values.length);
        }
    }
}
