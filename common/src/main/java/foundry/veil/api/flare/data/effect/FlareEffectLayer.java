package foundry.veil.api.flare.data.effect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.registry.PropertyModifierRegistry;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.flare.model.BakedShell;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.resources.ResourceLocation;
import foundry.veil.api.client.property.properties.TimeProperty;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.modifier.PropertyModifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FlareEffectLayer {
    public static final Codec<FlareEffectLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(FlareEffectLayer::getName),
            Codec.BOOL.optionalFieldOf("disabled", false).forGetter(FlareEffectLayer::isDisabled),
            FlareModel.CODEC.fieldOf("model").forGetter(FlareEffectLayer::getModel),
            CodecUtil.registryOrLegacyCodec(PropertyModifierRegistry.REGISTRY)
                    .<PropertyModifier<?>>dispatch(PropertyModifier::type, PropertyModifier::codec)
                        .listOf()
                        .optionalFieldOf("modifiers", new ArrayList<>())
                        .forGetter(FlareEffectLayer::getModifiers)
    ).apply(instance, FlareEffectLayer::new));

    private final String name;
    private final boolean disabled;
    private final FlareModel model;
    private final Map<String, List<PropertyModifier<?>>> modifiers;
    private final List<PropertyModifier<?>> originalModifiers;
    private final Map<String, Map<String, Property<?>>> properties;

    public FlareEffectLayer(String name, boolean disabled, FlareModel model, List<PropertyModifier<?>> modifiers) {
        this.name = name;
        this.disabled = disabled;
        this.model = model;
        this.originalModifiers = ImmutableList.copyOf(modifiers);
        Map<String, List<PropertyModifier<?>>> modifierMap = new HashMap<>();
        Map<String, Map<String, Property<?>>> properties = new HashMap<>();

        for (FlareMaterial material : model.getMaterials()) {
            Map<String, Property<?>> materialProperties = material.properties();
            this.putModelProperties(materialProperties);
            properties.put(material.clazz(), materialProperties);
        }

        for (PropertyModifier<?> modifier : modifiers) {
            List<PropertyModifier<?>> modifierList = modifierMap.computeIfAbsent(modifier.outputPropertyName(), n -> new ArrayList<>());
            modifierList.add(modifier);
        }

        modifierMap.replaceAll((k, v) -> ImmutableList.copyOf(v));
        this.modifiers = ImmutableMap.copyOf(modifierMap);

        this.properties = ImmutableMap.copyOf(properties);
    }

    public void putModelProperties(Map<String, Property<?>> materialProperties) {
        materialProperties.put(FlareModel.POSITION_PROPERTY_NAME, model.positionOffset);
        materialProperties.put(FlareModel.ROTATION_PROPERTY_NAME, model.rotationOffset);
        materialProperties.put(FlareModel.SCALE_PROPERTY_NAME, model.scaleOffset);
        materialProperties.put("ModelToWorld", model.modelToWorld);
        materialProperties.put("_Time", TimeProperty.INSTANCE);
    }

    public void render(EffectHost host, MatrixStack matrixStack, @Nullable Map<ResourceLocation, BakedShell> shellOverrides) {
        if (disabled) return;

        PropertyModifier.modifyProperty(host, null, model.positionOffset, modifiers.get(FlareModel.POSITION_PROPERTY_NAME));
        PropertyModifier.modifyProperty(host, null, model.rotationOffset, modifiers.get(FlareModel.ROTATION_PROPERTY_NAME));
        PropertyModifier.modifyProperty(host, null, model.scaleOffset, modifiers.get(FlareModel.SCALE_PROPERTY_NAME));

        model.render(host, matrixStack, modifiers, shellOverrides);

        model.positionOffset.resetOverrideValue();
        model.rotationOffset.resetOverrideValue();
        model.scaleOffset.resetOverrideValue();

    }

    public String getName() {
        return name;
    }

    public FlareModel getModel() {
        return model;
    }

    public List<PropertyModifier<?>> getModifiers() {
        return originalModifiers;
    }

    public boolean isDisabled() {
        return disabled;
    }
}
