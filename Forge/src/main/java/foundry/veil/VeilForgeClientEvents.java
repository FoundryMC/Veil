package foundry.veil;

import foundry.veil.postprocessing.PostProcessingHandler;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Veil.MODID)
public class VeilForgeClientEvents {

    @SubscribeEvent
    public static void onLevelRenderLast(RenderLevelStageEvent event){
        PostProcessingHandler.onLevelRenderLast(event.getPoseStack());
    }
}
