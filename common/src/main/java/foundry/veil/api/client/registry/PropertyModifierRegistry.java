package foundry.veil.api.client.registry;

import com.mojang.serialization.MapCodec;
import foundry.veil.Veil;
import foundry.veil.api.flare.data.FloatCurve;
import foundry.veil.api.flare.modifier.PropertyModifier;
import foundry.veil.api.flare.modifier.modifiers.FloatPropertyModifier;
import foundry.veil.api.flare.modifier.modifiers.Vec2PropertyModifier;
import foundry.veil.api.flare.modifier.modifiers.Vec3PropertyModifier;
import foundry.veil.api.flare.modifier.modifiers.Vec4PropertyModifier;
import foundry.veil.platform.registry.RegistrationProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.joml.*;

import java.util.function.Supplier;

/**
 * Registry for all property modifier types.
 * @since 2.5.0
 */
public final class PropertyModifierRegistry {

    public static final ResourceKey<Registry<PropertyModifierType<?,?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(Veil.veilPath("property_modifier"));
    private static final RegistrationProvider<PropertyModifierType<?,?>> PROVIDER = RegistrationProvider.get(REGISTRY_KEY, Veil.MODID);
    public static final Registry<PropertyModifierType<?, ?>> REGISTRY = PROVIDER.asVanillaRegistry();

    public static final Supplier<PropertyModifierType<Float, FloatPropertyModifier>> FLOAT = register(
            "float",
            PropertyModifier.createCodec(FloatPropertyModifier::new, FloatPropertyModifier::getCurve, FloatCurve.CODEC.optionalFieldOf("curve", FloatCurve.ZERO), 1)
    );
    public static final Supplier<PropertyModifierType<Vector2fc, Vec2PropertyModifier>> VEC2 = register(
            "vec2",
            PropertyModifier.createCodec(Vec2PropertyModifier::new, Vec2PropertyModifier::getCurves, FloatCurve.CODEC.listOf(0, 2).fieldOf("curves"), 2)
    );
    public static final Supplier<PropertyModifierType<Vector3fc, Vec3PropertyModifier>> VEC3 = register(
            "vec3",
            PropertyModifier.createCodec(Vec3PropertyModifier::new, Vec3PropertyModifier::getCurves, FloatCurve.CODEC.listOf(0, 3).fieldOf("curves"), 3)
    );
    public static final Supplier<PropertyModifierType<Vector4fc, Vec4PropertyModifier>> VEC4 = register(
            "vec4",
            PropertyModifier.createCodec(Vec4PropertyModifier::new, Vec4PropertyModifier::getCurves, FloatCurve.CODEC.listOf(0, 4).fieldOf("curves"), 4)
    );

    private static <T, M extends PropertyModifier<T>> Supplier<PropertyModifierType<T, M>> register(String name, MapCodec<M> codec) {
        return PROVIDER.register(name, () -> new PropertyModifierType<>(codec));
    }

    public record PropertyModifierType<T, M extends PropertyModifier<T>>(MapCodec<M> codec) {
    }

    private PropertyModifierRegistry() {
    }

    @ApiStatus.Internal
    public static void bootstrap() {
    }
}
