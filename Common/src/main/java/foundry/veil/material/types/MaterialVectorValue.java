package foundry.veil.material.types;

import com.mojang.math.Vector4f;
import foundry.veil.material.IMaterialField;

public class MaterialVectorValue implements IMaterialField<Vector4f> {
    Vector4f value;

    public MaterialVectorValue(Vector4f value) {
        this.value = value;
    }

    public MaterialVectorValue() {
        this.value = new Vector4f(0, 0, 0, 0);
    }
    @Override
    public Vector4f getValue() {
        return value;
    }

    @Override
    public void setValue(Vector4f value) {
        this.value = value;
    }

    @Override
    public MaterialFieldType getType() {
        return MaterialFieldType.VECTOR;
    }
}
