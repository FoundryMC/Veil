package foundry.veil.api.client.graveyard;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

import java.util.HashMap;
import java.util.Map;

public class AnimationProperties {
    private final Map<String, Object> objectProperties;
    private final Object2FloatMap<String> numberProperties;

    public AnimationProperties() {
        this.objectProperties = new HashMap<>();
        this.numberProperties = new Object2FloatOpenHashMap<>();
    }

    public void addProperty(String name, Object object) {
        this.objectProperties.put(name, object);
    }

    public void addProperty(String name, float value) {
        this.numberProperties.put(name, value);
    }

    public void setProperty(String name, Object object) {
        this.objectProperties.replace(name, object);
    }

    public void setProperty(String name, float value) {
        this.numberProperties.replace(name, value);
    }

    public Object getProperty(String name) {
        return this.objectProperties.get(name);
    }

    public float getNumProperty(String name) {
        return this.numberProperties.getFloat(name);
    }
}
