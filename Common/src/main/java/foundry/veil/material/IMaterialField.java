package foundry.veil.material;

import foundry.veil.material.types.MaterialFieldType;

public interface IMaterialField {
    Object getValue();
    void setValue(Object value);

    MaterialFieldType getType();

}
