package foundry.veil.color.theme;

public class StringThemeProperty implements IThemeProperty<String> {
    private String name;
    private String value;
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
    public String getValue() {
        return value;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
