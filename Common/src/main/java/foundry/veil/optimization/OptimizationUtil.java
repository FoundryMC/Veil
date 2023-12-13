package foundry.veil.optimization;

import net.minecraft.client.Minecraft;

public class OptimizationUtil {
    public static int STABLE_FPS = 60;

    public static void calculateStableFps() {
        // this is ran once every 10 seconds, and captures the fps at the time, and then averages it out between all the samples
        // this is to prevent the fps from being too low, allowing for more accurate animations
        int fps = Minecraft.getInstance().getFps();
        STABLE_FPS = (STABLE_FPS + fps) / 2;
    }

    public static int getStableFps() {
        return STABLE_FPS;
    }
}
