package foundry.veil.api.client.property.model;

import foundry.veil.api.client.property.InapplicableProperty;
import foundry.veil.api.client.property.ModelProperty;
import gg.moonflower.molangcompiler.api.MolangExpression;
import net.minecraft.util.Mth;
import foundry.veil.api.flare.modifier.PropertyModifier;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

/**
 * Vec3 property that specifies a rotation as a quaternion and as euler angles.
 *
 * @author GuyApooye
 */
@ModelProperty
@InapplicableProperty
public class RotationModelProperty extends Vec3ModelProperty {
    private final Quaternionfc rotation;
    private final Quaternionf overrideRotation;

    public RotationModelProperty(Vector3f value) {
        super(value);
        this.rotation = new Quaternionf().rotationXYZ(value.x() * Mth.DEG_TO_RAD, value.y() * Mth.DEG_TO_RAD, value.z() * Mth.DEG_TO_RAD);
        this.overrideRotation = new Quaternionf(this.rotation);
    }

    @Override
    public void modify(Vector3f value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
        super.modify(value, mode, optionalMolang);
        overrideRotation.rotationXYZ(this.overrideValue.x() * Mth.DEG_TO_RAD, this.overrideValue.y() * Mth.DEG_TO_RAD, this.overrideValue.z() * Mth.DEG_TO_RAD);
    }

    public Quaternionfc getRotation() {
        return overrideRotation;
    }

    @Override
    public void resetOverrideValue() {
        super.resetOverrideValue();
        overrideRotation.set(rotation);
    }
}
