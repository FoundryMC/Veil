package foundry.veil.neoforge;

import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.TickEvent;
import org.jetbrains.annotations.ApiStatus;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

@ApiStatus.Internal
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Veil.MODID, value = Dist.CLIENT)
public class VeilForgeClientEvents {

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            VeilClient.tickClient(Minecraft.getInstance().getFrameTime());
        }
    }

    @SubscribeEvent
    public static void keyPressed(InputEvent.Key event) {
        if (event.getAction() == GLFW_PRESS && VeilClient.EDITOR_KEY.matches(event.getKey(), event.getScanCode())) {
            VeilRenderSystem.renderer().getEditorManager().toggle();
        }
    }

    @SubscribeEvent
    public static void mousePressed(InputEvent.MouseButton.Pre event) {
        if (event.getAction() == GLFW_PRESS && VeilClient.EDITOR_KEY.matchesMouse(event.getButton())) {
            VeilRenderSystem.renderer().getEditorManager().toggle();
        }
    }

    @SubscribeEvent
    public static void leaveGame(ClientPlayerNetworkEvent.LoggingOut event) {
        VeilRenderSystem.renderer().getDeferredRenderer().reset();
    }
}
