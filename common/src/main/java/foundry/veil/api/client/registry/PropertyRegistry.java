package foundry.veil.api.client.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import foundry.veil.Veil;
import foundry.veil.api.client.property.properties.*;
import foundry.veil.platform.registry.RegistrationProvider;
import io.github.ocelot.glslprocessor.api.grammar.GlslTypeSpecifier;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.joml.*;

import java.util.function.Supplier;

/**
 * Registry for all property types.
 */
public final class PropertyRegistry {

    public static final ResourceKey<Registry<PropertyType<?,?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(Veil.veilPath("property"));
    private static final RegistrationProvider<PropertyType<?,?>> PROVIDER = RegistrationProvider.get(REGISTRY_KEY, Veil.MODID);
    public static final Registry<PropertyType<?, ?>> REGISTRY = PROVIDER.asVanillaRegistry();

    public static final Supplier<PropertyType<Float, FloatProperty>> FLOAT = register("float", Property.createCodec(FloatProperty::new, Codec.FLOAT), GlslTypeSpecifier.BuiltinType.FLOAT);
    public static final Supplier<PropertyType<Integer, IntProperty>> INT = register("int", Property.createCodec(IntProperty::new, Codec.INT), GlslTypeSpecifier.BuiltinType.INT);
    public static final Supplier<PropertyType<Boolean, BoolProperty>> BOOL = register("bool", Property.createCodec(BoolProperty::new, Codec.BOOL), GlslTypeSpecifier.BuiltinType.BOOL);
    public static final Supplier<PropertyType<Vector2f, Vec2Property>> VEC2 = register("vec2", Property.createCodec(Vec2Property::new, CodecUtil.VECTOR2F_CODEC), GlslTypeSpecifier.BuiltinType.VEC2);
    public static final Supplier<PropertyType<Vector3f, Vec3Property>> VEC3 = register("vec3", Property.createCodec(Vec3Property::new, CodecUtil.VECTOR3F_CODEC), GlslTypeSpecifier.BuiltinType.VEC3);
    public static final Supplier<PropertyType<Vector4f, Vec4Property>> VEC4 = register("vec4", Property.createCodec(Vec4Property::new, CodecUtil.VECTOR4F_CODEC), GlslTypeSpecifier.BuiltinType.VEC4);
    public static final Supplier<PropertyType<Matrix3f, Mat3Property>> MAT3 = register("mat3", Property.createCodec(Mat3Property::new, CodecUtil.MATRIX3F_CODEC), GlslTypeSpecifier.BuiltinType.MAT3);
    public static final Supplier<PropertyType<Matrix4f, Mat4Property>> MAT4 = register("mat4", Property.createCodec(Mat4Property::new, CodecUtil.MATRIX4F_CODEC), GlslTypeSpecifier.BuiltinType.MAT4);
    public static final Supplier<PropertyType<AbstractTexture, Sampler2DProperty>> SAMPLER2D = register("sampler2d", Sampler2DProperty.CODEC, GlslTypeSpecifier.BuiltinType.SAMPLER2D);

    private static <T, M extends Property<T>> Supplier<PropertyType<T, M>> register(String name, MapCodec<M> codec, GlslTypeSpecifier.BuiltinType glType) {
        return PROVIDER.register(name, () -> new PropertyType<>(codec, glType));
    }

    public record PropertyType<T, M extends Property<T>>(MapCodec<M> codec, GlslTypeSpecifier.BuiltinType glType) {
    }

    private PropertyRegistry() {
    }

    @ApiStatus.Internal
    public static void bootstrap() {
    }

}
