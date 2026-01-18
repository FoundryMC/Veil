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
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @since 2.5.0
 */
public final class FlareMaterial {
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
    private final String clazz;
    private final ResourceLocation renderTypeLocation;
    private final boolean randomizeSeed;
    private final Object2ObjectArrayMap<String, Property<?>> properties;
    private final List<Object2ObjectMap.Entry<String, Property<?>>> propertyEntries;
    
    
    public FlareMaterial(String clazz, ResourceLocation renderTypeLocation, boolean randomizeSeed, Map<String, Property<?>> properties) {
        properties.put("_ClipBrightness", new FloatProperty(1.0f));
        if (randomizeSeed) {
            properties.put("_Seed", RandomFloatProperty.INSTANCE);
        }
        this.clazz = clazz;
        this.renderTypeLocation = renderTypeLocation;
        this.randomizeSeed = randomizeSeed;
        this.properties = new Object2ObjectArrayMap<>(properties);
        this.propertyEntries = this.properties.object2ObjectEntrySet().stream().toList();
    }
    
    public void applyProperties(EffectHost host, @Nullable ShaderInstance shader, Map<String, List<PropertyModifier<?>>> modifiers) {
        if (shader == null) {
            return;
        }
        List<Object2ObjectMap.Entry<String, Property<?>>> entries = this.propertyEntries;
        for (int i = 0, size = entries.size(); i < size; i++) {
            Map.Entry<String, Property<?>> entry = entries.get(i);
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
        List<Object2ObjectMap.Entry<String, Property<?>>> entries = this.propertyEntries;
        for (int i = 0, size = entries.size(); i < size; i++) {
            Object2ObjectMap.Entry<String, Property<?>> entry = entries.get(i);
            Property<?> property = entry.getValue();
            if (property.getClass().getAnnotation(ModelProperty.class) != null) {
                continue;
            }
            property.resetOverrideValue();
        }
    }
    
    public String clazz() {
        return clazz;
    }
    
    public ResourceLocation renderTypeLocation() {
        return renderTypeLocation;
    }
    
    public boolean randomizeSeed() {
        return randomizeSeed;
    }
    
    public Map<String, Property<?>> properties() {
        return properties;
    }
    
}
