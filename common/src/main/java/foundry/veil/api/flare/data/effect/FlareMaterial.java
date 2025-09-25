package foundry.veil.api.flare.data.effect;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.property.InapplicableProperty;
import foundry.veil.api.client.property.InvertibleProperty;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.property.properties.RandomFloatProperty;
import foundry.veil.api.client.property.ModelProperty;
import foundry.veil.api.client.property.properties.FloatProperty;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.flare.modifier.PropertyModifier;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.resources.ResourceLocation;
import foundry.veil.api.flare.EffectHost;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record FlareMaterial(String clazz, ResourceLocation renderTypeLocation, boolean useRandomSeed,
                            Map<String, Property<?>> properties) {
    private static final Vector4f EMPTY_COLOR = new Vector4f();
    public static Codec<FlareMaterial> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("class").forGetter(FlareMaterial::clazz),
            ResourceLocation.CODEC.fieldOf("renderType").forGetter(FlareMaterial::renderTypeLocation),
            Codec.BOOL.optionalFieldOf("randomizeSeed", false).forGetter(FlareMaterial::useRandomSeed),
            Codec.mapPair(
                            Codec.STRING.fieldOf("name"),
                            CodecUtil.registryOrLegacyCodec(PropertyRegistry.REGISTRY)
                                    .<Property<?>>dispatchMap(Property::getType, Property::codec)
                    )
                    .codec()
                    .listOf()
                    .optionalFieldOf("properties", new ArrayList<>())
                    .forGetter(FlareMaterial::getListOfPairs)
    ).apply(instance, FlareMaterial::new));

    public FlareMaterial(String clazz, ResourceLocation renderTypeLocation, boolean useRandomSeed,
                         Map<String, Property<?>> properties) {
        this.clazz = clazz;
        this.renderTypeLocation = renderTypeLocation;
        this.useRandomSeed = useRandomSeed;
        this.properties = properties;
        properties.put("_ClipBrightness", new FloatProperty(1.0f));
        if (useRandomSeed) properties.put("_Seed", RandomFloatProperty.INSTANCE);
    }

    private FlareMaterial(String name, ResourceLocation renderType, Boolean random, List<Pair<String, Property<?>>> properties) {
        this(name, renderType, random, CodecUtil.pairListToMap(properties));
    }

    public void applyProperties(EffectHost host, ShaderProgram program, Map<String, List<PropertyModifier<?>>> modifiers) {
        if (program == null) return;
        for (Map.Entry<String, Property<?>> entry : properties.entrySet()) {
            String name = entry.getKey();
            Property<?> property = entry.getValue();

            if (property instanceof InapplicableProperty) continue;

            if (!(property instanceof ModelProperty))
                PropertyModifier.modifyProperty(host, clazz, property, modifiers.get(name));

            property.applyValue(name, program);

            if (property instanceof InvertibleProperty<?> invertibleProperty)
                invertibleProperty.applyInverseValue(name, program);
        }
    }

    public void resetProperties(EffectHost host, ShaderProgram program) {
        if (program == null) return;
        for (Property<?> property : properties.values()) {
            if (property instanceof ModelProperty) continue;
            property.resetOverrideValue();
        }

    }

    private static List<Pair<String, Property<?>>> getListOfPairs(FlareMaterial material) {
        return CodecUtil.entrySetToPairList(material.properties.entrySet());
    }

}
