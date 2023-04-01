package foundry.veil;

import foundry.veil.postprocessing.DynamicEffectInstance;
import foundry.veil.postprocessing.InstantiatedPostProcessor;
import foundry.veil.postprocessing.PostProcessingHandler;
import foundry.veil.postprocessing.PostProcessor;
import foundry.veil.test.BloomPostProcessor;
import foundry.veil.test.PostProcessingEffectsRegistry;
import foundry.veil.ui.VeilIGuiOverlay;
import foundry.veil.ui.VeilUITooltipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Veil.MODID, value = Dist.CLIENT)
public class VeilForgeClientEvents {
    public static final IGuiOverlay OVERLAY = VeilUITooltipRenderer::renderOverlay;
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void onLevelRenderLast(RenderLevelLastEvent event){
//
//        PostProcessingHandler.onLevelRenderLast(event.getPoseStack());
//    }

    @SubscribeEvent
    public static void renderStage(RenderLevelStageEvent event){
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER){
            PostProcessingHandler.onLevelRenderLast(event.getPoseStack());
        }
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event){
        if(event.getAction() == GLFW.GLFW_PRESS){
            if(event.getKey() == GLFW_KEY_COMMA){
                PostProcessingHandler.getInstances().stream().filter(instance -> instance instanceof PostProcessor).forEach(instance -> {
                    instance.setActive(false);
                });
            }
            if(event.getKey() == GLFW_KEY_PERIOD){
                PostProcessingHandler.getInstances().stream().filter(instance -> instance instanceof PostProcessor).forEach(instance -> {
                    instance.init();
                    instance.setActive(true);
                });
            }
            if(event.getKey() == GLFW_KEY_P){
                PostProcessingEffectsRegistry.INSTANCES.forEach(instantiatedPostProcessor -> instantiatedPostProcessor.getFxInstances().forEach(DynamicEffectInstance::remove));
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Removed all effects"));
            }
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event){
        if(event.phase == TickEvent.Phase.END){
            if(Minecraft.getInstance().player == null)
                return;
            VeilClient.tickClient(Minecraft.getInstance().player.tickCount, Minecraft.getInstance().getFrameTime());
        }
    }

}
