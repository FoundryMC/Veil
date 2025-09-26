package foundry.veil.api.flare.modifier.modifiers;

import foundry.veil.api.client.registry.PropertyModifierRegistry;
import foundry.veil.api.flare.data.FloatCurve;
import foundry.veil.api.flare.modifier.Controller;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Vec4PropertyModifier extends PropertyModifier<Vector4fc> {

    protected final List<FloatCurve> curveList;
    protected final Vector4f dummy;
    public final FloatCurve curveX;
    public final FloatCurve curveY;
    public final FloatCurve curveZ;
    public final FloatCurve curveW;

    public Vec4PropertyModifier(String name, String clazz, String inputControllerName, String outputPropertyName, PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang, List<FloatCurve> curveList) {
        super(PropertyModifierRegistry.VEC4.get(), name, clazz, inputControllerName, outputPropertyName, mode, optionalMolang);
        dummy = new Vector4f();
        this.curveList = curveList;
        this.curveX = curveList.get(0);
        this.curveY = curveList.get(1);
        this.curveZ = curveList.get(2);
        this.curveW = curveList.get(3);
    }

    @Override
    public Vector4fc get(Controller controller) {
        float value = controller.getValue();
        dummy.x = curveX.evaluate(value);
        dummy.y = curveY.evaluate(value);
        dummy.z = curveZ.evaluate(value);
        dummy.w = curveW.evaluate(value);
        return dummy;
    }

    public List<FloatCurve> getCurves() {
        return new ArrayList<>(curveList);
    }
}
