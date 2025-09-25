package foundry.veil.api.client.property.model;

import foundry.veil.api.client.property.InapplicableProperty;
import foundry.veil.api.client.property.ModelProperty;
import foundry.veil.api.client.property.properties.Vec3Property;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Vec3ModelProperty extends Vec3Property implements ModelProperty, InapplicableProperty {

    public Vec3ModelProperty(Vector3f value) {
        super(value);
    }

    public Vector3fc getValue() {
        return this.overrideValue;
    }

}
