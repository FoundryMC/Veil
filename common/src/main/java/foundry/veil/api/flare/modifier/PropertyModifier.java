package foundry.veil.api.flare.modifier;

import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.property.ImmutableProperty;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.registry.PropertyModifierRegistry;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.FlareEffectManager;
import foundry.veil.api.molang.MolangExpressionCodec;
import gg.moonflower.molangcompiler.api.MolangExpression;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static foundry.veil.Veil.LOGGER;

/**
 * @since 2.5.0
 */
public abstract class PropertyModifier<T> {

    private final PropertyModifierRegistry.PropertyModifierType<T, ?> type;
    private final String name;
    private final String clazz;
    private final String inputControllerName;
    private final PropertyModifierMode mode;
    private final String outputPropertyName;
    private final Optional<List<MolangExpression>> optionalMolang;

    public PropertyModifier(
            PropertyModifierRegistry.PropertyModifierType<T, ?> type,
            String name,
            @Nullable String clazz,
            String inputControllerName,
            String outputPropertyName,
            PropertyModifierMode mode,
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
        ControllerManager controllerManager = FlareEffectManager.getInstance().getControllerManager();
        Controller controller = controllerManager.getOrCreateController(this.inputControllerName, host);

        try {
            property.modify((A) this.get(controller), this.mode, this.optionalMolang);
        } catch (Exception e) {
            LOGGER.error("Could not modify property {} for controller {}", this.outputPropertyName, this.inputControllerName);
        }
    }

    public String name() {
        return this.name;
    }

    public @Nullable String clazz() {
        return this.clazz;
    }

    public Optional<String> optionalClazz() {
        return Optional.ofNullable(this.clazz);
    }

    public String inputControllerName() {
        return this.inputControllerName;
    }

    public PropertyModifierMode mode() {
        return this.mode;
    }
    
    @ApiStatus.Experimental
    public Optional<List<MolangExpression>> molangExpressions() {
        return this.optionalMolang;
    }

    public String outputPropertyName() {
        return this.outputPropertyName;
    }

    public PropertyModifierRegistry.PropertyModifierType<T, ?> type() {
        return this.type;
    }

    public Pair<Optional<List<MolangExpression>>, PropertyModifierMode> getPair() {
        return Pair.of(this.optionalMolang, this.mode);
    }

    private static PropertyModifierMode modeFromPair(Pair<Optional<List<MolangExpression>>, PropertyModifierMode> expression) {
        if (expression.getFirst().isPresent() && !expression.getFirst().get().isEmpty()) {
            return PropertyModifierMode.MOLANG;
        }
        return expression.getSecond();
    }

    private static MapCodec<? extends Pair<Optional<List<MolangExpression>>, PropertyModifierMode>> pairCodecFromMode(PropertyModifierMode mode, int size) {
        if (mode == PropertyModifierMode.MOLANG) {
            return Codec.mapPair(MolangExpressionCodec.CODEC.listOf(size, size).optionalFieldOf("molang"), MapCodec.unit(mode));
        }
        return MapCodec.unit(Pair.of(Optional.empty(), mode));
    }

    public static void modifyProperty(EffectHost host, @Nullable String clazz, Property<?> property, Iterable<PropertyModifier<?>> modifiers) {
        if (modifiers == null) {
            return;
        }
        if (property.getClass().getAnnotation(ImmutableProperty.class) != null) {
            return;
        }
        for (PropertyModifier<?> modifier : modifiers) {
            if (clazz != null && !Objects.equals(clazz, modifier.clazz)) {
                continue;
            }
            modifier.apply(host, property);
        }
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
        @ApiStatus.Experimental
        MOLANG("molang"); //Buggy, don't use.

        private final String name;

        PropertyModifierMode(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}
