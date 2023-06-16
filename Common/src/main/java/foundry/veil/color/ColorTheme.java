package foundry.veil.color;

import foundry.veil.color.theme.IThemeProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A color theme is a collection of colors. The colors can be accessed by name. Themes are intended to be used for color schemes.
 * <p>
 *     A color theme can be used to apply a color scheme to a {@link foundry.veil.ui.Tooltippable} tooltip.
 *     Themes can also be used to hold arbitrary color data mapped to strings.
 */
public class ColorTheme {
    private Map<Optional<String>, Color> colors = new HashMap<>();
    private Map<Optional<String>, IThemeProperty<?>> properties = new HashMap<>();

    public ColorTheme(){

    }

    public ColorTheme(Color... colors){
        for(Color color : colors){
            addColor(color);
        }
    }

    public void addProperty(String name, IThemeProperty<?> property){
        properties.put(Optional.ofNullable(name), property);
    }

    public void addProperty(IThemeProperty<?> property){
        properties.put(Optional.empty(), property);
    }

    public Object getAndCastProperty(String name){
        IThemeProperty<?> property = properties.get(Optional.ofNullable(name));
        return property.getType().cast(property);
    }

    public IThemeProperty<?> getProperty(String name){
        return properties.get(Optional.ofNullable(name));
    }

    public void removeProperty(String name){
        properties.remove(Optional.ofNullable(name));
    }

    public void clearProperties(){
        properties.clear();
    }

    public void addColor(String name, Color color){
        colors.put(Optional.ofNullable(name), color);
    }

    public void addColor(Color color){
        colors.put(Optional.empty(), color);
    }

    public Color getColor(String name){
        return colors.get(Optional.ofNullable(name));
    }

    public Color getColor(){
        return colors.get(Optional.empty());
    }

    public void removeColor(String name){
        colors.remove(Optional.ofNullable(name));
    }

    public void removeColor(){
        colors.remove(Optional.empty());
    }

    public void clear(){
        colors.clear();
    }

    public List<String> getNames(){
        return (List<String>) colors.keySet().stream().map(Optional::get).toList();
    }

    public List<Color> getColors(){
        return (List<Color>) colors.values();
    }

    public Map<Optional<String>, Color> getColorsMap(){
        return colors;
    }
}
