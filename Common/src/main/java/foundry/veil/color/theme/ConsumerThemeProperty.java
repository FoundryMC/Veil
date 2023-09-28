package foundry.veil.color.theme;

import java.util.function.Consumer;

public class ConsumerThemeProperty implements IThemeProperty<Consumer<?>>{
    private String name;
    private Consumer<?> value;
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if(name == null){
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
    }

    @Override
    public Consumer<?> getValue() {
        return value;
    }

    @Override
    public Class<?> getType() {
        return ConsumerThemeProperty.class;
    }

    public void setValue(Consumer<?> value) {
        this.value = value;
    }
}
