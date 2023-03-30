package foundry.veil.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;

@FunctionalInterface
public interface VeilIGuiOverlay
{
    void render(Gui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight);
}
