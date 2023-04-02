package foundry.veil;

import com.mojang.math.Vector3f;
import foundry.veil.test.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

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
                    Vector3f pos = new Vector3f(event.getEntity().position());
                    PostProcessingEffectsRegistry.BLOOM.addFxInstance(new BloomFx(List.of(
                            () -> Minecraft.getInstance().player.bob,
                            () -> (float)Minecraft.getInstance().player.totalExperience,
                            () -> (float)Minecraft.getInstance().player.experienceLevel,
                            () -> Minecraft.getInstance().player.sprintTime+0.0f,
                            () -> Minecraft.getInstance().player.getHealth(),
                            () -> Minecraft.getInstance().player.getMaxHealth(),
                            () -> Minecraft.getInstance().player.getAbsorptionAmount(),
                            pos::x,
                            pos::y,
                            pos::z
                    )));
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
