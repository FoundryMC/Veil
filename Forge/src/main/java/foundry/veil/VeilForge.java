package foundry.veil;

import com.mojang.math.Vector3f;
import foundry.veil.math.Easings;
import foundry.veil.test.BloomFx;
import foundry.veil.test.EnergyScanFx;
import foundry.veil.test.EnergySphereFx;
import foundry.veil.test.PostProcessingEffectsRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
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
            if(event.getItemStack().getItem().equals(Items.ALLIUM)){
                if(event.getLevel().isClientSide){
                    PostProcessingEffectsRegistry.BLOOM.addFxInstance(new BloomFx(){
                    });
//                    PostProcessingEffectsRegistry.ENERGY_SPHERE.addFxInstance(new EnergySphereFx(new Vector3f(0,100,0), 0, 1){
//                        @Override
//                        public void update(double deltaTime) {
//                            super.update(deltaTime);
//
//                            float t = getTime() / 7.5F;
//
//                            if (t > 1) {
//                                remove();
//                                return;
//                            }
//                            t = 0.5f;;
//
//                            this.radius = t * 300F;
//                            this.intensity = (300F - radius) / 300F;
//                            this.intensity = (float) Mth.clamp(intensity, 0., 1.);
//                        }
//                    });
//                    PostProcessingEffectsRegistry.ENERGY_SCAN.addFxInstance(new EnergyScanFx(new Vector3f(0,100,0)) {
//                        @Override
//                        public void update(double deltaTime) {
//                            super.update(deltaTime);
//
//                            float t = getTime() / 7.5F;
//                            if (t < 1) {
//                                t = 0.5f;
//                            }
//
//                            virtualRadius = t * 300F;
//                            if (virtualRadius > 1300F) {
//                                remove();
//                            }
//                        }
//                    });
                }
            }
        }
    }
}
