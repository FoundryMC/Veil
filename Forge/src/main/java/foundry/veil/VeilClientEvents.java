package foundry.veil;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static foundry.veil.VeilForgeClientEvents.OVERLAY;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = Veil.MODID)
public class VeilClientEvents {

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event){
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "uitooltip", OVERLAY);
    }


}
