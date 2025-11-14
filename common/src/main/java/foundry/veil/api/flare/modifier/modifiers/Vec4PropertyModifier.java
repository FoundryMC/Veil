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

/**
 * @since 2.5.0
 */
public class Vec4PropertyModifier extends PropertyModifier<Vector4fc> {

    protected final List<FloatCurve> curveList;
    protected final Vector4f dummy;
    public final FloatCurve curveX;
    public final FloatCurve curveY;
    public final FloatCurve curveZ;
    public final FloatCurve curveW;

    public Vec4PropertyModifier(String name, String clazz, String inputControllerName, String outputPropertyName, PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang, List<FloatCurve> curveList) {
        super(PropertyModifierRegistry.VEC4.get(), name, clazz, inputControllerName, outputPropertyName, mode, optionalMolang);
        this.dummy = new Vector4f();
        this.curveList = curveList;
        this.curveX = !curveList.isEmpty() ? curveList.get(0) : FloatCurve.ZERO;
        this.curveY = curveList.size() > 1 ? curveList.get(1) : FloatCurve.ZERO;
        this.curveZ = curveList.size() > 2 ? curveList.get(2) : FloatCurve.ZERO;
        this.curveW = curveList.size() > 3 ? curveList.get(3) : FloatCurve.ZERO;
    }

    @Override
    public Vector4fc get(Controller controller) {
        float value = controller.getValue();
        this.dummy.x = this.curveX.evaluate(value);
        this.dummy.y = this.curveY.evaluate(value);
        this.dummy.z = this.curveZ.evaluate(value);
        this.dummy.w = this.curveW.evaluate(value);
        return this.dummy;
    }

    public List<FloatCurve> getCurves() {
        return new ArrayList<>(this.curveList);
    }
}
