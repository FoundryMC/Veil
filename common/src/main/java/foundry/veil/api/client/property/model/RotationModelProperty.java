package foundry.veil.api.client.property.model;

import foundry.veil.api.client.property.InapplicableProperty;
import gg.moonflower.molangcompiler.api.MolangExpression;
import net.minecraft.util.Mth;
import foundry.veil.api.flare.modifier.PropertyModifier;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class RotationModelProperty extends Vec3ModelProperty {
    private final ThreadLocal<Quaternionf> rotationOverride = new ThreadLocal<>();
    private final Quaternionf rotation = new Quaternionf();

    public RotationModelProperty(Vector3f value) {
        super(value);
        rotation.rotationXYZ(value.x() * Mth.DEG_TO_RAD, value.y() * Mth.DEG_TO_RAD, value.z() * Mth.DEG_TO_RAD);
    }

    @Override
    public void modify(Vector3f value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
        super.modify(value, mode, optionalMolang);
        rotation.rotationXYZ(this.overrideValue.x() * Mth.DEG_TO_RAD, this.overrideValue.y() * Mth.DEG_TO_RAD, this.overrideValue.z() * Mth.DEG_TO_RAD);
    }

    public Quaternionfc getRotation() {
        if (rotationOverride.get() != null) return rotationOverride.get();
        return rotation;
    }
}
