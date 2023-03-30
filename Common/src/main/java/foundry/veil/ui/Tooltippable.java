package foundry.veil.ui;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface Tooltippable {
    List<Component> getTooltip();
    void setTooltip(List<Component> tooltip);
    void addTooltip(Component tooltip);
    void addTooltip(List<Component> tooltip);
    void addTooltip(String tooltip);
    int[] getTheme();
    void setTheme(int[] theme);
    void setBackgroundColor(int color);
    void setTopBorderColor(int color);
    void setBottomBorderColor(int color);

}
