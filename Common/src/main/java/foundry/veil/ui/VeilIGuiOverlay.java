package foundry.veil.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface VeilIGuiOverlay
{
    void render(Gui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight);
}
