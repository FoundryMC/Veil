package foundry.veil.api.flare.modifier.modifiers;

import foundry.veil.api.client.registry.PropertyModifierRegistry;
import foundry.veil.api.flare.data.FloatCurve;
import foundry.veil.api.flare.modifier.Controller;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @since 2.5.0
 */
public class Vec3PropertyModifier extends PropertyModifier<Vector3fc> {

    protected final List<FloatCurve> curveList;
    protected final Vector3f dummy;

    public final FloatCurve curveX;
    public final FloatCurve curveY;
    public final FloatCurve curveZ;

    public Vec3PropertyModifier(String name, String clazz, String inputControllerName, String outputPropertyName, PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang, List<FloatCurve> curveList) {
        super(PropertyModifierRegistry.VEC3.get(), name, clazz, inputControllerName, outputPropertyName, mode, optionalMolang);
        this.dummy = new Vector3f();
        this.curveList = curveList;
        this.curveX = !curveList.isEmpty() ? curveList.get(0) : FloatCurve.ZERO;
        this.curveY = curveList.size() > 1 ? curveList.get(1) : FloatCurve.ZERO;
        this.curveZ = curveList.size() > 2 ? curveList.get(2) : FloatCurve.ZERO;
    }

    @Override
    public Vector3fc get(Controller controller) {
        float value = controller.getValue();
        this.dummy.x = this.curveX.evaluate(value);
        this.dummy.y = this.curveY.evaluate(value);
        this.dummy.z = this.curveZ.evaluate(value);
        return this.dummy;
    }

    public List<FloatCurve> getCurves() {
        return new ArrayList<>(this.curveList);
    }
}
