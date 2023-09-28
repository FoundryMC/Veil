package foundry.veil.color.theme;

public class NumberThemeProperty implements IThemeProperty<Number>{
    private String name;
    private Number value;
    private Class<?> type = NumberThemeProperty.class;
    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {
        if(name == null){
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
    }

    @Override
    public Number getValue() {
        return value;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    public void setValue(Number value) {
        this.value = value;
    }

    public void setType(Class<?> type) {
        // check if type is a subclass of Number
        if(Number.class.isAssignableFrom(type)){
            this.type = type;
        }else{
            throw new IllegalArgumentException("Type must be a subclass of Number");
        }
    }

    // return the value and cast it to the specified type
    public <T extends Number> T getValue(Class<T> type) {
        return type.cast(value);
    }
}
