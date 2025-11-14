package foundry.veil.api.flare.modifier.modifiers;

import foundry.veil.api.client.registry.PropertyModifierRegistry;
import foundry.veil.api.flare.data.FloatCurve;
import foundry.veil.api.flare.modifier.Controller;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;

import java.util.List;
import java.util.Optional;

/**
 * @since 2.5.0
 */
public class FloatPropertyModifier extends PropertyModifier<Float> {

    public final FloatCurve curve;

    public FloatPropertyModifier(String name, String clazz, String inputControllerName, String outputPropertyName, PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang, FloatCurve curve) {
        super(PropertyModifierRegistry.FLOAT.get(), name, clazz, inputControllerName, outputPropertyName, mode, optionalMolang);
        this.curve = curve;
    }

    public FloatCurve getCurve() {
        return this.curve;
    }

    @Override
    public Float get(Controller controller) {
        return this.curve.evaluate(controller.getValue());
    }
}
