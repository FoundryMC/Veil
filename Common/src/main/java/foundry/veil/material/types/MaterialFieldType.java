package foundry.veil.material.types;

import foundry.veil.material.IMaterialField;

import java.util.function.Consumer;

public enum MaterialFieldType {
    SLIDER,
    COLOR,
    TEXTURE,
    VECTOR,
    VALUE;

    public IMaterialField createField() {
        return switch (this) {
            case SLIDER -> new MaterialSliderField();
            case COLOR -> new MaterialColorField();
            case TEXTURE -> new MaterialTextureField();
            case VECTOR -> new MaterialVectorValue();
            case VALUE -> new MaterialValueField();
            default -> null;
        };
    }
}
