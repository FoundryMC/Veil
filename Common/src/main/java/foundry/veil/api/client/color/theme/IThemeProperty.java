package foundry.veil.api.client.color.theme;

/**
 * @author amo
 */
public interface IThemeProperty<T> {

    String getName();

    void setName(String name);

    T getValue();

    Class<?> getType();
}
