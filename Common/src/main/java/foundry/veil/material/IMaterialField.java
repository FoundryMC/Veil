package foundry.veil.material;

import foundry.veil.material.types.MaterialFieldType;

public interface IMaterialField<T> {
    T getValue();
    void setValue(T value);

    MaterialFieldType getType();
}
