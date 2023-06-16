package foundry.veil.color.theme;

public interface IThemeProperty<T> {
    String getName();
    void setName(String name);
    abstract T getValue();
    Class<?> getType();
}
