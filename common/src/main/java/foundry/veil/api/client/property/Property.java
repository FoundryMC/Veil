package foundry.veil.api.client.property;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import foundry.veil.api.flare.modifier.PropertyModifier;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Property<T> {

    protected T overrideValue;
    protected final T value;
    private final PropertyRegistry.PropertyType<T, ? extends Property<T>> type;
    private final Supplier<MolangEnvironment> environment;

    public Property(PropertyRegistry.PropertyType<T, ? extends Property<T>> type, T value) {
        this.type = type;
        this.value = value;
        this.overrideValue = cloneValue(value);
        this.environment = Suppliers.memoize(() -> {
            MolangRuntime.Builder builder = MolangRuntime.runtime();
            setQueries(builder);
            return builder.create();
        });
    }

    protected void setQueries(MolangRuntime.Builder builder) {
    }

    public void applyValue(String name, ShaderProgram shader) {
        this.applyValue(shader.getOrCreateUniform(name), shader.getUniformLocation(name));
    }
    public abstract void applyValue(ShaderUniformAccess uniformAccess, int location);
    public abstract void modify(T value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang);
    protected abstract T cloneValue(T value);

    public void resetOverrideValue() {
        this.overrideValue = cloneValue(value);
    }

    public PropertyRegistry.PropertyType<T, ? extends Property<T>> getType() {
        return type;
    }

    public static <T, M extends Property<T>> MapCodec<M> codec(PropertyRegistry.PropertyType<T, M> type) {
        return type.codec();
    }

    public static <T, M extends Property<T>> MapCodec<M> createCodec(Function<T, M> factory, Codec<T> typeCodec) {
        return typeCodec.fieldOf("value").xmap(factory, property -> property.value);
    }

    public Supplier<MolangEnvironment> getEnvironment() {
        return environment;
    }
}
