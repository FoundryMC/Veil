package foundry.veil.api.client.property.modifiers;

import foundry.veil.api.client.registry.PropertyModifierRegistry;
import foundry.veil.api.flare.data.FloatCurve;
import foundry.veil.api.flare.modifier.Controller;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Vec2PropertyModifier extends PropertyModifier<Vector2fc> {

    protected final List<FloatCurve> curveList;
    protected final Vector2f dummy;
    public final FloatCurve curveX;
    public final FloatCurve curveY;

    public Vec2PropertyModifier(String name, String clazz, String inputControllerName, String outputPropertyName, PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang, List<FloatCurve> curveList) {
        super(PropertyModifierRegistry.VEC2.get(), name, clazz, inputControllerName, outputPropertyName, mode, optionalMolang);
        dummy = new Vector2f();
        this.curveList = curveList;
        this.curveX = curveList.get(0);
        this.curveY = curveList.get(1);
    }

    @Override
    public Vector2fc get(Controller controller) {
        float value = controller.getValue();
        dummy.x = curveX.evaluate(value);
        dummy.y = curveY.evaluate(value);
        return dummy;
    }

    public List<FloatCurve> getCurves() {
        return new ArrayList<>(curveList);
    }
}
