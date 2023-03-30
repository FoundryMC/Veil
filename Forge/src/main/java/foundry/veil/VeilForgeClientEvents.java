package foundry.veil;

import foundry.veil.postprocessing.InstantiatedPostProcessor;
import foundry.veil.postprocessing.PostProcessingHandler;
import foundry.veil.test.BloomPostProcessor;
import foundry.veil.test.PostProcessingEffectsRegistry;
import foundry.veil.ui.VeilIGuiOverlay;
import foundry.veil.ui.VeilUITooltipRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_COMMA;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_L;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Veil.MODID, value = Dist.CLIENT)
public class VeilForgeClientEvents {
    public static final IGuiOverlay OVERLAY = VeilUITooltipRenderer::renderOverlay;
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLevelRenderLast(RenderLevelLastEvent event){
        PostProcessingHandler.onLevelRenderLast(event.getPoseStack());
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event){
        if(event.getAction() == GLFW.GLFW_PRESS){
            if(event.getKey() == GLFW_KEY_COMMA){
                PostProcessingHandler.getInstances().forEach(instance -> instance.setActive(false));
            }
            if(event.getKey() == GLFW_KEY_L){
                PostProcessingHandler.getInstances().stream().filter(instance -> instance instanceof BloomPostProcessor).forEach(instance -> instance.setActive(true));
            }
        }
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event){
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "uitooltip", OVERLAY);
    }
}
