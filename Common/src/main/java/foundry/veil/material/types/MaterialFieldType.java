package foundry.veil.material.types;

import foundry.veil.material.IMaterialField;

import java.util.function.Consumer;

public enum MaterialFieldType {
    SLIDER(MaterialSliderField.class),
    COLOR(MaterialColorField.class),
    TEXTURE(MaterialTextureField.class),
    VECTOR(MaterialVectorValue.class),
    VALUE(MaterialValueField.class);

    Class<?> type;

    MaterialFieldType(Class<?> type) {
        this.type = type;
    }

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

    public Class<?> getType() {
        return this.type;
    }
}
