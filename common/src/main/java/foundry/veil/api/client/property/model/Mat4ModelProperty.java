package foundry.veil.api.client.property.model;

import foundry.veil.api.client.property.ModelProperty;
import foundry.veil.api.client.property.properties.Mat4Property;
import org.joml.Matrix4f;

@ModelProperty
public class Mat4ModelProperty extends Mat4Property {

    public Mat4ModelProperty(Matrix4f value) {
        super(value);
    }
}
