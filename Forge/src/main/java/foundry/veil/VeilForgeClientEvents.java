package foundry.veil;

import foundry.veil.postprocessing.ExpandedShaderInstance;
import foundry.veil.postprocessing.InstantiatedPostProcessor;
import foundry.veil.postprocessing.PostProcessingHandler;
import foundry.veil.test.PostProcessingEffectsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_COMMA;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_L;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Veil.MODID, value = Dist.CLIENT)
public class VeilForgeClientEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLevelRenderLast(RenderLevelLastEvent event){
        PostProcessingHandler.onLevelRenderLast(event.getPoseStack());
        ((ExpandedShaderInstance) Minecraft.getInstance().gameRenderer.blitShader).getPerspectiveUniform();
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event){
        if(event.getAction() == GLFW.GLFW_PRESS){
            if(event.getKey() == GLFW_KEY_COMMA){
                PostProcessingHandler.getInstances().forEach(instance -> instance.setActive(false));
            }
        }
    }
}
