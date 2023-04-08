package foundry.veil.material.types;

import foundry.veil.material.IMaterialField;

public class MaterialValueField implements IMaterialField<Object> {
    private Object value;

    public MaterialValueField(Object value) {
        this.value = value;
    }

    public MaterialValueField() {
        this.value = "";
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public MaterialFieldType getType() {
        return MaterialFieldType.VALUE;
    }
}
