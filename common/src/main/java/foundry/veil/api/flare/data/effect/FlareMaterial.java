package foundry.veil.api.flare.data.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.property.InapplicableProperty;
import foundry.veil.api.client.property.InvertibleProperty;
import foundry.veil.api.client.property.ModelProperty;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.property.properties.FloatProperty;
import foundry.veil.api.client.property.properties.RandomFloatProperty;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.modifier.PropertyModifier;
import foundry.veil.api.util.CodecUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @since 2.5.0
 */
public record FlareMaterial(
        String clazz,
        ResourceLocation renderTypeLocation,
        boolean randomizeSeed,
        Map<String, Property<?>> properties
) {
    public static Codec<FlareMaterial> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("class").forGetter(FlareMaterial::clazz),
            ResourceLocation.CODEC.fieldOf("renderType").forGetter(FlareMaterial::renderTypeLocation),
            Codec.BOOL.optionalFieldOf("randomizeSeed", false).forGetter(FlareMaterial::randomizeSeed),
            Codec.unboundedMap(
                            Codec.STRING,
                            CodecUtil.registryOrLegacyCodec(PropertyRegistry.REGISTRY)
                                    .<Property<?>>dispatchMap(Property::getType, Property::codec)
                                    .codec()
                    )
                    .optionalFieldOf("properties", Map.of())
                    .forGetter(FlareMaterial::properties)
    ).apply(instance, (clazz, renderType, randomizeSeed, properties) -> new FlareMaterial(clazz, renderType, randomizeSeed, new Object2ObjectArrayMap<>(properties))));

    public FlareMaterial {
        properties.put("_ClipBrightness", new FloatProperty(1.0f));
        if (randomizeSeed) {
            properties.put("_Seed", RandomFloatProperty.INSTANCE);
        }
    }

    public void applyProperties(EffectHost host, @Nullable ShaderInstance shader, Map<String, List<PropertyModifier<?>>> modifiers) {
        if (shader == null) {
            return;
        }
        for (Map.Entry<String, Property<?>> entry : this.properties.entrySet()) {
            Property<?> property = entry.getValue();
            Class<?> propertyClass = property.getClass();

            if (propertyClass.getAnnotation(InapplicableProperty.class) != null) {
                continue;
            }

            String name = entry.getKey();
            if (propertyClass.getAnnotation(ModelProperty.class) == null) {
                PropertyModifier.modifyProperty(host, this.clazz, property, modifiers.get(name));
            }

            property.applyValue(name, shader);
            if (property instanceof InvertibleProperty<?> invertibleProperty) {
                invertibleProperty.applyInverseValue(name, shader);
            }
        }
    }

    public void resetProperties(EffectHost host, @Nullable ShaderInstance shader) {
        if (shader == null) {
            return;
        }
        for (Property<?> property : this.properties.values()) {
            if (property.getClass().getAnnotation(ModelProperty.class) != null) {
                continue;
            }
            property.resetOverrideValue();
        }
    }
}
