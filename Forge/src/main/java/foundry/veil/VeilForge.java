package foundry.veil;

import com.mojang.math.Vector3f;
import foundry.veil.test.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(Veil.MODID)
public class VeilForge {
    public VeilForge() {
        Veil.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> VeilForgeClient::init);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Veil.MODID)
    public static class VeilForgeEvents {
        @SubscribeEvent
        public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
            if (event.getItemStack().getItem().equals(Items.ALLIUM)) {
                if (event.getLevel().isClientSide) {
                    PostProcessingEffectsRegistry.BLOOM.addFxInstance(new BloomFx(() -> {
                        return Minecraft.getInstance().level.isDay() ? 0.1f : (float)(Math.sin((Minecraft.getInstance().level.getGameTime()+Minecraft.getInstance().getPartialTick())/2f)+1) * 0.1f;
                    }, () -> {
                        return Minecraft.getInstance().level.isDay() ? 0.1f/512f : 2.0f/512f;
                    }));
                }
            } else if (event.getItemStack().getItem().equals(Items.POPPY)) {
                if (event.getLevel().isClientSide) {
                    PostProcessingEffectsRegistry.OUTLINE.addFxInstance(new OutlineFx(new Vector3f(Vec3.atCenterOf(event.getEntity().getOnPos()))) {
                    });

                }
            } else if (event.getItemStack().getItem().equals(Items.AZURE_BLUET)) {
                if (event.getLevel().isClientSide) {
                    PostProcessingEffectsRegistry.SCANLINE.addFxInstance(new BasicFx() {
                    });
                }
            } else if (event.getItemStack().getItem().equals(Items.OXEYE_DAISY)) {
                if (event.getLevel().isClientSide) {
                    PostProcessingEffectsRegistry.AREA.addFxInstance(new AreaFx(new Vector3f(Vec3.atCenterOf(event.getEntity().getOnPos()))) {
                    });
                }
            }
        }
    }
}
