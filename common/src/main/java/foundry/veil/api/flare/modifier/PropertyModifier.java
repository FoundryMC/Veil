package foundry.veil.api.flare.modifier;


import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.registry.PropertyModifierRegistry;
import foundry.veil.api.molang.MolangExpressionCodec;
import gg.moonflower.molangcompiler.api.MolangExpression;
import io.github.ocelot.glslprocessor.api.grammar.GlslTypeSpecifier;
import net.minecraft.util.StringRepresentable;
import foundry.veil.api.client.property.ImmutableProperty;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.FlareEffectManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static foundry.veil.Veil.LOGGER;

public abstract class PropertyModifier<T> {

    private final String name;
    private final @Nullable String clazz;
    private final String inputControllerName;
    private final PropertyModifierMode mode;
    private final String outputPropertyName;
    private final Optional<List<MolangExpression>> optionalMolang;
    private final PropertyModifierRegistry.PropertyModifierType<T, ?> type;

    public PropertyModifier(PropertyModifierRegistry.PropertyModifierType<T, ?> type, String name, String clazz, String inputControllerName, String outputPropertyName, PropertyModifierMode mode,
                            Optional<List<MolangExpression>> optionalMolang) {
        this.type = type;
        this.name = name;
        this.clazz = clazz;
        this.inputControllerName = inputControllerName;
        this.mode = mode;
        this.outputPropertyName = outputPropertyName;
        this.optionalMolang = optionalMolang;
    }

    public abstract T get(Controller controller);

    @SuppressWarnings("unchecked")
    public <A> void apply(@NotNull EffectHost host, Property<A> property) {

        Controller controller = FlareEffectManager.getInstance().getControllerManager().
                getOrCreateController(inputControllerName, host);

        try {
            property.modify((A) get(controller), mode, optionalMolang);
        } catch (Exception e) {
            LOGGER.error("Could not modify property {} for controller {}", outputPropertyName, inputControllerName);
        }
    }

    public String name() {
        return name;
    }
    public @Nullable String clazz() {
        return clazz;
    }

    public Optional<String> optionalClazz() {
        return Optional.ofNullable(clazz);
    }

    public String inputControllerName() {
        return inputControllerName;
    }

    public PropertyModifierMode mode() {
        return mode;
    }

    public Optional<List<MolangExpression>> molangExpressions() {
        return optionalMolang;
    }

    public String outputPropertyName() {
        return outputPropertyName;
    }

    public PropertyModifierRegistry.PropertyModifierType<T, ?> type() {
        return type;
    }

    public Pair<Optional<List<MolangExpression>>, PropertyModifierMode> getPair() {
        return Pair.of(optionalMolang, mode);
    }

    private static PropertyModifierMode modeFromPair(Pair<Optional<List<MolangExpression>>, PropertyModifierMode> expression) {
        if (expression.getFirst().isPresent() && !expression.getFirst().get().isEmpty())
            return PropertyModifierMode.MOLANG;
        return expression.getSecond();
    }

    private static MapCodec<? extends Pair<Optional<List<MolangExpression>>, PropertyModifierMode>> pairCodecFromMode(PropertyModifierMode mode, int size) {
        if (mode == PropertyModifierMode.MOLANG)
            return Codec.mapPair(MolangExpressionCodec.CODEC.listOf(size, size).optionalFieldOf("molang"), MapCodec.unit(mode));
        return MapCodec.unit(Pair.of(Optional.empty(), mode));
    }

    public static void modifyProperty(EffectHost host, @Nullable String clazz, Property<?> property, List<PropertyModifier<?>> modifiers) {
        if (modifiers == null) return;
        if (property instanceof ImmutableProperty) return;
        for (PropertyModifier<?> modifier : modifiers) {
            if (!(modifier.clazz == null || clazz == null || modifier.clazz.equals(clazz))) continue;
            modifier.apply(host, property);
        }
    }

    public static <T, M extends PropertyModifier<T>> MapCodec<M> codec(PropertyModifierRegistry.PropertyModifierType<T,M> type) {
        return type.codec();
    }

    public static <A, T extends PropertyModifier<A>> MapCodec<T> createCodec(Function6<String, String, String, String, PropertyModifierMode, Optional<List<MolangExpression>>, T> factory, int molangSize) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(PropertyModifier::name),
                Codec.STRING.optionalFieldOf("class").forGetter(PropertyModifier::optionalClazz),
                Codec.STRING.fieldOf("controller").forGetter(PropertyModifier::inputControllerName),
                Codec.STRING.fieldOf("property").forGetter(PropertyModifier::outputPropertyName),
                StringRepresentable.fromValues(PropertyModifierMode::values).<Pair<Optional<List<MolangExpression>>, PropertyModifierMode>>dispatchMap("mode",
                        PropertyModifier::modeFromPair,
                        mode -> pairCodecFromMode(mode, molangSize)
                ).forGetter(PropertyModifier::getPair)

        ).apply(instance, (name, clazz, controller, property, pair) -> factory.apply(name, clazz.orElse(null), controller, property, pair.getSecond(), pair.getFirst())));
    }

    public static <A, T extends PropertyModifier<A>, O> MapCodec<T> createCodec(Function7<String, String, String, String, PropertyModifierMode, Optional<List<MolangExpression>>, O, T> factory, Function<T, O> supplier, MapCodec<O> additionalCodec, int molangSize) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(PropertyModifier::name),
                Codec.STRING.optionalFieldOf("class").forGetter(PropertyModifier::optionalClazz),
                Codec.STRING.fieldOf("controller").forGetter(PropertyModifier::inputControllerName),
                Codec.STRING.fieldOf("property").forGetter(PropertyModifier::outputPropertyName),
                StringRepresentable.fromValues(PropertyModifierMode::values).<Pair<Optional<List<MolangExpression>>, PropertyModifierMode>>dispatchMap("mode",
                        PropertyModifier::modeFromPair,
                        mode -> pairCodecFromMode(mode, molangSize)
                ).forGetter(PropertyModifier::getPair),
                additionalCodec.forGetter(supplier)

        ).apply(instance, (name, clazz, controller, property, pair, additionalInformation) -> factory.apply(name, clazz.orElse(null), controller, property, pair.getSecond(), pair.getFirst(), additionalInformation)));
    }

    public enum PropertyModifierMode implements StringRepresentable {
        REPLACE("replace"),
        ADD("add"),
        SUBTRACT("subtract"),
        MULTIPLY("multiply"),
        MOLANG("molang");

        private final String name;

        PropertyModifierMode(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

}
