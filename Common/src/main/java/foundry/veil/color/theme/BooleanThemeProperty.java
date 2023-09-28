package foundry.veil.color.theme;

public class BooleanThemeProperty implements IThemeProperty<Boolean> {
    private String name;
    private boolean value;
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
    public Boolean getValue() {
        return value;
    }
    public void setValue(boolean value) {
        this.value = (boolean) value;
    }

    @Override
    public Class<?> getType() {
        return BooleanThemeProperty.class;
    }
}
