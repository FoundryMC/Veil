package foundry.veil.color;

public class Filter {
    public static Color apply(Color color, float value, IFilterType type) {
        return type.apply(color, value);
    }

    public enum FilterType implements IFilterType {
        sepia {
            @Override
            public Color apply(Color color, float value) {
                float r = (color.getRed() * .393f) + (color.getGreen() * .769f) + (color.getBlue() * .189f);
                float g = (color.getRed() * .349f) + (color.getGreen() * .686f) + (color.getBlue() * .168f);
                float b = (color.getRed() * .272f) + (color.getGreen() * .534f) + (color.getBlue() * .131f);
                return new Color(r, g, b, color.getAlpha());
            }
        },
        grayscale {
            @Override
            public Color apply(Color color, float value) {
                float average = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0f;
                return new Color(average, average, average, color.getAlpha());
            }
        }
    }
    public interface IFilterType {
        Color apply(Color color, float value);
    }
}
