package foundry.veil.model.graveyard.update;

import java.util.HashMap;
import java.util.Map;

public class AnimationProperties {
    Map<String, Object> objectProperties;
    Map<String, Float> numberProperties;

    public AnimationProperties() {
        this.objectProperties = new HashMap<>();
        this.numberProperties = new HashMap<>();
    }

    public void addProperty(String name, Object object) {
        objectProperties.put(name, object);
    }

    public void addProperty(String name, float value) {
        numberProperties.put(name, value);
    }

    public void setProperty(String name, Object object) {
        objectProperties.replace(name, object);
    }

    public void setProperty(String name, float value) {
        numberProperties.replace(name, value);
    }

    public Object getProperty(String name) {
        return objectProperties.get(name);
    }

    public float getNumProperty(String name) {
        return numberProperties.get(name);
    }
}
